import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Logica_Caballo {

    static final int N = 8; //tamaño del tablero
    static final int TOTAL_CABALLOS = 8; //cantidad de caballos que se deben ubicar

    static final int[] dx = {2, 1, -1, -2, -2, -1, 1, 2}; //movimientos que puede hacer el caballo en x
    static final int[] dy = {1, 2, 2, 1, -1, -2, -2, -1}; //movimientos que puede hacer el caballo en y

    static class Estado {
        int[][] tablero; //declarar un tablero temporal
        int caballosColocados; //cantidad de caballos que se han colocado en el tablero

        Estado(int[][] tablero, int caballosColocados) {
            this.tablero = tablero;
            this.caballosColocados = caballosColocados;
        }

        //hacer una copia del tablero por cada posición
        Estado copiar() {
            int[][] nuevoTablero = new int[N][N];
            for (int i = 0; i < N; i++)
                System.arraycopy(tablero[i], 0, nuevoTablero[i], 0, N); //copiar el tablero
            return new Estado(nuevoTablero, caballosColocados); //retornar el tablero
        }
    }

    static class Posicion {
        int fila; //fila de la casilla
        int columna; //columna de la casilla

        Posicion(int fila, int columna) {
            this.fila = fila;
            this.columna = columna;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int[][] tablero = new int[N][N]; //crear el tablero principal
        for (int[] fila : tablero)
            Arrays.fill(fila, 0); //llenar el tablero inicialmente con 0, que significa casilla vacía

        System.out.println("Problema de los 8 caballos");
        System.out.println("1. Iniciar con tablero vacío");
        System.out.println("2. Iniciar con un caballo ya ubicado");
        System.out.print("Seleccione una opción: ");
        int opcion = scanner.nextInt();

        int caballosIniciales = 0; //cantidad de caballos que ya están puestos al iniciar

        if (opcion == 2) {
            System.out.print("Ingrese la fila inicial del caballo (0 a 7): ");
            int fila = scanner.nextInt();

            System.out.print("Ingrese la columna inicial del caballo (0 a 7): ");
            int columna = scanner.nextInt();

            if (fila >= 0 && fila < N && columna >= 0 && columna < N) {
                tablero[fila][columna] = 1; //ubicar el caballo inicial en el tablero
                caballosIniciales = 1; //ya hay un caballo colocado
            } else {
                System.out.println("La posición ingresada no es válida.");
                return;
            }
        } else if (opcion != 1) {
            System.out.println("Opción no válida.");
            return;
        }

        Estado estadoInicial = new Estado(tablero, caballosIniciales); //iniciar el estado del tablero

        int continuar = 1;

        while (continuar == 1) {
            Estado intento = estadoInicial.copiar(); //crear una copia del tablero inicial para generar una nueva solución

            System.out.println("\nEstado inicial:");
            imprimir(intento.tablero);

            if (resolverDesde(intento)) {
                System.out.println("\nSolución encontrada:");
                imprimir(intento.tablero);
            } else {
                System.out.println("No se encontró solución.");
            }

            System.out.println("\n¿Desea generar otra solución?");
            System.out.println("1. Sí");
            System.out.println("2. No");
            System.out.print("Seleccione una opción: ");
            continuar = scanner.nextInt();
        }
    }

    //metodo con el que se busca encontrar una forma valida de ubicar los 8 caballos
    static boolean resolverDesde(Estado estado) {
        if (estado.caballosColocados == TOTAL_CABALLOS) { //caso base: ya se ubicaron los 8 caballos
            return true;
        }

        List<Posicion> posiciones = obtenerPosicionesAleatorias(); //obtener todas las posiciones del tablero en orden aleatorio

        for (Posicion posicion : posiciones) { //recorrer cada posición posible del tablero

            int fila = posicion.fila; //fila que se va a probar
            int columna = posicion.columna; //columna que se va a probar

            //verificar si es valido ubicar un caballo en esa posición
            if (esSeguro(fila, columna, estado.tablero)) {
                estado.tablero[fila][columna] = 1; //si es valido ubicamos el caballo
                estado.caballosColocados++; //sumamos un caballo colocado

                //volvemos a llamar el metodo para intentar ubicar el siguiente caballo
                if (resolverDesde(estado))
                    return true;

                //por si esa posición no lleva a una solución, quitamos el caballo y probamos otra posición
                //backtrack
                estado.tablero[fila][columna] = 0;
                estado.caballosColocados--;
            }
        }

        return false;
    }

    // ── Método nuevo: igual que resolverDesde pero guarda cada paso para la animación ──
    // No modifica la lógica original, solo agrega la lista de pasos como parámetro extra
    static boolean resolverConPasos(Estado estado, List<int[][]> pasos) {
        if (estado.caballosColocados == TOTAL_CABALLOS) { //caso base: ya se ubicaron los 8 caballos
            pasos.add(copiarTablero(estado.tablero)); //guardar el estado final
            return true;
        }

        List<Posicion> posiciones = obtenerPosicionesAleatorias(); //obtener todas las posiciones del tablero en orden aleatorio

        for (Posicion posicion : posiciones) { //recorrer cada posición posible del tablero

            int fila = posicion.fila; //fila que se va a probar
            int columna = posicion.columna; //columna que se va a probar

            //verificar si es valido ubicar un caballo en esa posición
            if (esSeguro(fila, columna, estado.tablero)) {
                estado.tablero[fila][columna] = 1; //si es valido ubicamos el caballo
                estado.caballosColocados++; //sumamos un caballo colocado
                pasos.add(copiarTablero(estado.tablero)); //guardar este paso

                //volvemos a llamar el metodo para intentar ubicar el siguiente caballo
                if (resolverConPasos(estado, pasos))
                    return true;

                //backtrack
                estado.tablero[fila][columna] = 0;
                estado.caballosColocados--;
                pasos.add(copiarTablero(estado.tablero)); //guardar el backtrack
            }
        }

        return false;
    }

    //crear una lista con todas las posiciones del tablero y mezclarlas para que la solución pueda cambiar
    static List<Posicion> obtenerPosicionesAleatorias() {
        List<Posicion> posiciones = new ArrayList<>(); //lista donde se guardan las posiciones del tablero

        for (int fila = 0; fila < N; fila++) {
            for (int columna = 0; columna < N; columna++) {
                posiciones.add(new Posicion(fila, columna)); //agregar una posición a la lista
            }
        }

        Collections.shuffle(posiciones); //mezclar las posiciones para probarlas en orden diferente
        return posiciones; //retornar la lista mezclada
    }

    //verificar que el movimiento si sea valido y que ningún caballo ataque a otro
    static boolean esSeguro(int x, int y, int[][] tablero) {
        if (tablero[x][y] == 1) { //si ya hay un caballo en esa casilla, no se puede ubicar otro
            return false;
        }

        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i]; //posición que podría atacar un caballo en x
            int ny = y + dy[i]; //posición que podría atacar un caballo en y

            if (nx >= 0 && ny >= 0 && nx < N && ny < N && tablero[nx][ny] == 1) {
                return false; //si hay un caballo que puede atacar esta posición, no es seguro
            }
        }

        return true; //si ningún caballo ataca esta posición, es seguro
    }

    //mostrar el tablero
    static void imprimir(int[][] tablero) {
        for (int[] fila : tablero) {
            for (int celda : fila) {
                if (celda == 1) {
                    System.out.print(" C "); //C representa un caballo
                } else {
                    System.out.print(" . "); //punto representa una casilla vacía
                }
            }
            System.out.println();
        }
    }

    // Utilidad interna para copiar el tablero (usada por resolverConPasos)
    static int[][] copiarTablero(int[][] tablero) {
        int[][] copia = new int[N][N];
        for (int i = 0; i < N; i++)
            System.arraycopy(tablero[i], 0, copia[i], 0, N);
        return copia;
    }
}
