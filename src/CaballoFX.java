import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class CaballoFX extends Application {

    // ── Constantes tomadas de Logica_Caballo ─────────────────────────────────
    static final int N              = Logica_Caballo.N;
    static final int TOTAL_CABALLOS = Logica_Caballo.TOTAL_CABALLOS;
    static final int[] dx           = Logica_Caballo.dx;
    static final int[] dy           = Logica_Caballo.dy;

    // ── Tamaño visual ─────────────────────────────────────────────────────────
    static final int CELL = 72;

    // ── Paleta ────────────────────────────────────────────────────────────────
    static final Color BG         = Color.web("#0d0f14");
    static final Color PANEL_BG   = Color.web("#13161e");
    static final Color LIGHT_CELL = Color.web("#e8d5b0");
    static final Color DARK_CELL  = Color.web("#8b5e3c");
    static final Color ACCENT     = Color.web("#f0c050");
    static final Color ACCENT2    = Color.web("#4ecdc4");
    static final Color KNIGHT_CLR = Color.web("#f5f0e8");
    static final Color SHADOW_CLR = Color.web("#1a0a00");

    // ── Estado UI ─────────────────────────────────────────────────────────────
    private Rectangle[][] cells     = new Rectangle[N][N];
    private Text[][]      symbols   = new Text[N][N];
    private StackPane[][] cellPanes = new StackPane[N][N];
    private int[][]       board     = new int[N][N];
    private int     initialRow  = -1;
    private int     initialCol  = -1;
    private boolean placingMode = false;

    private Label       statusLabel;
    private Label       counterLabel;
    private Label       solutionCount;
    private int         solutionNumber = 0;
    private GridPane    boardGrid;
    private Button      solveBtn;
    private Button      newSolBtn;
    private Button      clearBtn;
    private Button      placeBtn;
    private ProgressBar progressBar;
    private Timeline    solveAnimation;
    private CheckBox    showAttacksCheck;

    // ═════════════════════════════════════════════════════════════════════════
    @Override
    public void start(Stage stage) {
        stage.setTitle("♞ Problema de los 8 Caballos");
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BG, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setTop(buildHeader());

        StackPane boardWrapper = new StackPane(buildBoard());
        boardWrapper.setPadding(new Insets(20, 20, 20, 30));
        root.setCenter(boardWrapper);
        root.setRight(buildSidebar());

        Scene scene = new Scene(root, N * CELL + 280, N * CELL + 120);
        stage.setScene(scene);
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(700), boardGrid);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(22, 30, 14, 30));
        box.setBackground(new Background(new BackgroundFill(
                Color.web("#0a0c10"), CornerRadii.EMPTY, Insets.EMPTY)));

        Text icon = new Text("♞");
        icon.setFont(Font.font("serif", 34));
        icon.setFill(ACCENT);
        icon.setEffect(new DropShadow(12, ACCENT.deriveColor(0, 1, 1, 0.6)));

        VBox titles = new VBox(2);
        Text title = new Text("Problema de los 8 Caballos");
        title.setFont(Font.font("serif", FontWeight.BOLD, 22));
        title.setFill(LIGHT_CELL);
        Text sub = new Text("Backtracking con visualización paso a paso");
        sub.setFont(Font.font("monospace", 11));
        sub.setFill(Color.web("#888"));
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        solutionCount = new Label("Solución #0");
        solutionCount.setFont(Font.font("monospace", FontWeight.BOLD, 14));
        solutionCount.setTextFill(ACCENT2);
        solutionCount.setPadding(new Insets(4, 12, 4, 12));
        solutionCount.setBackground(new Background(new BackgroundFill(
                Color.web("#1a2a28"), new CornerRadii(6), Insets.EMPTY)));

        box.getChildren().addAll(icon, titles, spacer, solutionCount);
        return box;
    }

    // ── Tablero ───────────────────────────────────────────────────────────────
    private GridPane buildBoard() {
        boardGrid = new GridPane();
        boardGrid.setEffect(new DropShadow(30, Color.BLACK));

        for (int c = 0; c < N; c++) {
            Text lbl = new Text(String.valueOf((char) ('a' + c)));
            lbl.setFont(Font.font("monospace", 11));
            lbl.setFill(Color.web("#666"));
            StackPane sp = new StackPane(lbl);
            sp.setMinSize(CELL, 18);
            sp.setAlignment(Pos.CENTER);
            boardGrid.add(sp, c + 1, 0);
        }
        for (int r = 0; r < N; r++) {
            Text lbl = new Text(String.valueOf(8 - r));
            lbl.setFont(Font.font("monospace", 11));
            lbl.setFill(Color.web("#666"));
            StackPane sp = new StackPane(lbl);
            sp.setMinSize(18, CELL);
            sp.setAlignment(Pos.CENTER_RIGHT);
            sp.setPadding(new Insets(0, 4, 0, 0));
            boardGrid.add(sp, 0, r + 1);
        }

        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                Rectangle rect = new Rectangle(CELL, CELL);
                rect.setFill((row + col) % 2 == 0 ? LIGHT_CELL : DARK_CELL);
                cells[row][col] = rect;

                Text sym = new Text("");
                sym.setFont(Font.font("serif", 34));
                sym.setFill(KNIGHT_CLR);
                sym.setEffect(new DropShadow(6, SHADOW_CLR));
                symbols[row][col] = sym;

                StackPane pane = new StackPane(rect, sym);
                pane.setMinSize(CELL, CELL);
                cellPanes[row][col] = pane;

                final int r = row, c = col;
                pane.setOnMouseClicked(e -> handleCellClick(r, c));
                pane.setOnMouseEntered(e -> { if (placingMode) rect.setOpacity(0.75); });
                pane.setOnMouseExited(e -> rect.setOpacity(1.0));

                boardGrid.add(pane, col + 1, row + 1);
            }
        }
        return boardGrid;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24, 22, 24, 14));
        box.setPrefWidth(240);
        box.setBackground(new Background(new BackgroundFill(PANEL_BG, CornerRadii.EMPTY, Insets.EMPTY)));

        placeBtn = actionButton("📍  Colocar caballo inicial", false);
        placeBtn.setOnAction(e -> togglePlaceMode());

        solveBtn = actionButton("♟  Resolver", false);
        solveBtn.setOnAction(e -> solve(false));

        newSolBtn = actionButton("🔀  Nueva solución", true);
        newSolBtn.setDisable(true);
        newSolBtn.setOnAction(e -> solve(true));

        clearBtn = actionButton("✕  Limpiar tablero", true);
        clearBtn.setOnAction(e -> clearBoard());

        showAttacksCheck = new CheckBox("Mostrar casillas atacadas");
        showAttacksCheck.setTextFill(Color.web("#ccc"));
        showAttacksCheck.setFont(Font.font("monospace", 12));
        showAttacksCheck.setSelected(true);
        showAttacksCheck.setOnAction(e -> refreshBoardColors());

        statusLabel = new Label("Listo. Selecciona una opción.");
        statusLabel.setTextFill(Color.web("#aaa"));
        statusLabel.setFont(Font.font("monospace", 12));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(200);

        counterLabel = new Label("Caballos: 0 / 8");
        counterLabel.setTextFill(ACCENT);
        counterLabel.setFont(Font.font("monospace", FontWeight.BOLD, 14));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: #f0c050;");

        VBox legend = new VBox(6,
                legendItem(ACCENT,                                        "Caballo colocado"),
                legendItem(Color.web("#ff4444").deriveColor(0,1,1,0.35), "Casilla atacada"),
                legendItem(Color.web("#4ecdc4").deriveColor(0,1,1,0.3),  "Casilla segura"),
                legendItem(Color.web("#4a9eff").deriveColor(0,1,1,0.5),  "Caballo inicial")
        );

        box.getChildren().addAll(
                sectionLabel("CONTROLES"),
                placeBtn, solveBtn, newSolBtn, clearBtn,
                separator(),
                sectionLabel("OPCIONES"),
                showAttacksCheck,
                separator(),
                sectionLabel("ESTADO"),
                counterLabel, statusLabel, progressBar,
                separator(),
                sectionLabel("LEYENDA"),
                legend
        );
        return box;
    }

    // ── Helpers visuales ─────────────────────────────────────────────────────
    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("monospace", FontWeight.BOLD, 10));
        l.setTextFill(Color.web("#555"));
        l.setPadding(new Insets(4, 0, 0, 0));
        return l;
    }

    private Separator separator() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #2a2d35;");
        return s;
    }

    private Button actionButton(String text, boolean secondary) {
        Button b = new Button(text);
        b.setFont(Font.font("monospace", FontWeight.BOLD, 12));
        b.setPrefWidth(200);
        b.setPadding(new Insets(10, 14, 10, 14));
        b.setAlignment(Pos.CENTER_LEFT);
        String base   = secondary ? "#1e2330" : "#2d2400";
        String border = secondary ? "#3a3f50" : "#f0c050";
        String fg     = secondary ? "#aaa"    : "#f0c050";
        String style  = String.format(
                "-fx-background-color:%s;-fx-border-color:%s;-fx-border-width:1;" +
                        "-fx-border-radius:6;-fx-background-radius:6;-fx-text-fill:%s;-fx-cursor:hand;",
                base, border, fg);
        b.setStyle(style);
        b.setOnMouseEntered(e -> b.setStyle(style.replace(base, secondary ? "#252a3a" : "#3d3000")));
        b.setOnMouseExited(e -> b.setStyle(style));
        return b;
    }

    private HBox legendItem(Color color, String label) {
        Rectangle rect = new Rectangle(14, 14);
        rect.setFill(color);
        rect.setArcWidth(3); rect.setArcHeight(3);
        Label l = new Label(label);
        l.setTextFill(Color.web("#999"));
        l.setFont(Font.font("monospace", 11));
        HBox h = new HBox(8, rect, l);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ── Clic en celda ─────────────────────────────────────────────────────────
    private void handleCellClick(int row, int col) {
        if (!placingMode) return;
        clearBoard();
        initialRow = row;
        initialCol = col;
        board[row][col] = 1;
        placingMode = false;
        placeBtn.setText("📍  Colocar caballo inicial");
        boardGrid.setStyle("-fx-cursor: default;");
        refreshBoardColors();
        updateCounter();
        setStatus("Caballo inicial en (" + (char)('a' + col) + (8 - row) + "). ¡Listo para resolver!");
    }

    private void togglePlaceMode() {
        placingMode = !placingMode;
        if (placingMode) {
            placeBtn.setText("✕  Cancelar colocación");
            setStatus("Haz clic en una casilla para colocar el caballo inicial.");
            boardGrid.setStyle("-fx-cursor: crosshair;");
        } else {
            placeBtn.setText("📍  Colocar caballo inicial");
            setStatus("Modo de colocación cancelado.");
            boardGrid.setStyle("-fx-cursor: default;");
        }
    }

    // ── Resolver — usa Logica_Caballo.Estado y Logica_Caballo.resolverConPasos ─
    private void solve(boolean shuffle) {
        if (solveAnimation != null) solveAnimation.stop();

        // Construir estado inicial con la clase Estado de Logica_Caballo
        int[][] tableroInicial = new int[N][N];
        int caballosIniciales = 0;
        if (initialRow >= 0) {
            tableroInicial[initialRow][initialCol] = 1;
            caballosIniciales = 1;
        }
        Logica_Caballo.Estado estadoInicial =
                new Logica_Caballo.Estado(tableroInicial, caballosIniciales);

        solveBtn.setDisable(true);
        newSolBtn.setDisable(true);
        clearBtn.setDisable(true);
        placeBtn.setDisable(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        setStatus("Calculando solución…");

        Thread t = new Thread(() -> {
            // Copiar el estado (usando el copiar() de Logica_Caballo.Estado)
            Logica_Caballo.Estado intento = estadoInicial.copiar();
            List<int[][]> pasos = new ArrayList<>();

            // Llamar al método de Logica_Caballo que captura cada paso del backtracking
            boolean found = Logica_Caballo.resolverConPasos(intento, pasos);

            Platform.runLater(() -> {
                if (found) {
                    solutionNumber++;
                    solutionCount.setText("Solución #" + solutionNumber);
                    animateSolution(pasos);
                } else {
                    progressBar.setProgress(0);
                    setStatus("❌ No se encontró solución.");
                    enableButtons();
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ── Animación: muestra solo los pasos donde se agrega un caballo ──────────
    private void animateSolution(List<int[][]> pasos) {
        // Filtrar solo frames donde el número de caballos crece (ignorar backtracks)
        List<int[][]> keyframes = new ArrayList<>();
        keyframes.add(new int[N][N]); // tablero vacío como arranque visual
        int prev = 0;
        for (int[][] paso : pasos) {
            int cnt = contarCaballos(paso);
            if (cnt > prev) { keyframes.add(paso); prev = cnt; }
        }

        solveAnimation = new Timeline();
        for (int i = 0; i < keyframes.size(); i++) {
            final int[][] estado = keyframes.get(i);
            KeyFrame kf = new KeyFrame(Duration.millis(i * 450L), e -> {
                // Usar copiarTablero de Logica_Caballo para actualizar el board
                board = Logica_Caballo.copiarTablero(estado);
                refreshBoardColors();
                updateCounter();
                int cnt = contarCaballos(estado);
                progressBar.setProgress((double) cnt / TOTAL_CABALLOS);
                if (cnt == TOTAL_CABALLOS) {
                    setStatus("✅ ¡Solución encontrada! 8 caballos sin atacarse.");
                    pulseAllKnights();
                } else {
                    setStatus("Colocando caballo " + cnt + " de 8…");
                }
            });
            solveAnimation.getKeyFrames().add(kf);
        }
        solveAnimation.setOnFinished(e -> enableButtons());
        solveAnimation.play();
    }

    // ── Colores — usa dx/dy de Logica_Caballo para calcular ataques ──────────
    private void refreshBoardColors() {
        boolean showAtk = showAttacksCheck.isSelected();
        boolean[][] attacked = new boolean[N][N];
        if (showAtk)
            for (int r = 0; r < N; r++)
                for (int c = 0; c < N; c++)
                    if (board[r][c] == 1) marcarAtaques(r, c, attacked);

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                Color base = (r + c) % 2 == 0 ? LIGHT_CELL : DARK_CELL;
                if (board[r][c] == 1) {
                    boolean isInitial = (r == initialRow && c == initialCol);
                    cells[r][c].setFill(isInitial
                            ? Color.web("#4a9eff").interpolate(base, 0.3) : base);
                    symbols[r][c].setText("♞");
                    symbols[r][c].setFill(isInitial ? Color.web("#aad4ff") : KNIGHT_CLR);
                    symbols[r][c].setEffect(new DropShadow(14,
                            isInitial ? Color.web("#4a9eff") : ACCENT));
                } else {
                    symbols[r][c].setText("");
                    symbols[r][c].setEffect(null);
                    if (showAtk && attacked[r][c])
                        cells[r][c].setFill(base.interpolate(Color.web("#ff4444"), 0.28));
                    else if (showAtk)
                        cells[r][c].setFill(base.interpolate(Color.web("#4ecdc4"), 0.18));
                    else
                        cells[r][c].setFill(base);
                }
            }
        }
    }

    // Usa dx/dy de Logica_Caballo (referenciados arriba como constantes de clase)
    private void marcarAtaques(int kr, int kc, boolean[][] attacked) {
        for (int i = 0; i < 8; i++) {
            int nr = kr + dx[i], nc = kc + dy[i];
            if (nr >= 0 && nc >= 0 && nr < N && nc < N)
                attacked[nr][nc] = true;
        }
    }

    private void pulseAllKnights() {
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++)
                if (board[r][c] == 1) {
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), symbols[r][c]);
                    st.setFromX(1.0); st.setFromY(1.0);
                    st.setToX(1.25); st.setToY(1.25);
                    st.setCycleCount(2); st.setAutoReverse(true);
                    st.setDelay(Duration.millis((r * N + c) * 40));
                    st.play();
                }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────
    private void clearBoard() {
        if (solveAnimation != null) solveAnimation.stop();
        board = new int[N][N];
        initialRow = -1; initialCol = -1;
        solutionNumber = 0;
        solutionCount.setText("Solución #0");
        progressBar.setProgress(0);
        newSolBtn.setDisable(true);
        refreshBoardColors();
        updateCounter();
        setStatus("Tablero limpio. Listo.");
    }

    private void enableButtons() {
        solveBtn.setDisable(false);
        newSolBtn.setDisable(false);
        clearBtn.setDisable(false);
        placeBtn.setDisable(false);
    }

    private void updateCounter() {
        counterLabel.setText("Caballos: " + contarCaballos(board) + " / 8");
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }

    private int contarCaballos(int[][] b) {
        int cnt = 0;
        for (int[] row : b) for (int v : row) if (v == 1) cnt++;
        return cnt;
    }

    public static void main(String[] args) { launch(args); }
}
