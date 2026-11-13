package es.ugr.ddsi.seminario1;

import java.util.Scanner;

/**
 * Único Scanner sobre System.in para todo el programa.
 *
 * Si hay dos Scanner leyendo de System.in, el primero se guarda en su buffer
 * varias líneas a la vez y el segundo se queda sin ellas (pasa al pegar varias
 * líneas en la consola o al redirigir la entrada desde un fichero).
 * No se cierra nunca: cerrarlo cerraría System.in.
 */
public final class Entrada {

    public static final Scanner SC = new Scanner(System.in);

    private Entrada() {
    }
}
