package com.fullbuster.literalura.principal;

import com.fullbuster.literalura.model.Libro;
import com.fullbuster.literalura.model.Autor;
import com.fullbuster.literalura.model.Datos;
import com.fullbuster.literalura.model.DatosLibro;
import com.fullbuster.literalura.repository.AutorRepository;
import com.fullbuster.literalura.repository.LibroRepository;
import com.fullbuster.literalura.service.ConsumoAPI;
import com.fullbuster.literalura.service.ConvierteDatos;

import java.util.*;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;

public class Principal {

    private Scanner read =new Scanner(System.in);
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository libroRepositorio;
    private AutorRepository autorRepositorio;
    private Optional<Autor> autorEncontrado;
    private List<Libro> libros;
    private List<Autor> autores;
    private String idiomas[] = {"es", "en", "fr", "pt"};

    public Principal(AutorRepository repositorioDeAutor, LibroRepository repositorioDeLibros) {
        this.autorRepositorio = repositorioDeAutor;
        this.libroRepositorio = repositorioDeLibros;

    }

    public void mostrarMenu() {
        var opcion = -1;
        while (opcion != 0) {
            System.out.println("\n");
            var menu = """
                    ********                                               ********
                    **                  Bienvenido a Literalura                  **
                    ********                                               ********
                    **  Selecciona una opción del menú                           **
                    **  1 - Buscar y agregar un libro a la base de datos         **
                    **  2 - Listar los libros buscados                           **
                    **  3 - Listar los autores                                   **
                    **  4 - Listado de autores vivos en un determinado año       **
                    **  5 - Listado de libros por idioma                         **
                    **  6 - Obtener estadísticas de la base de datos             **
                    **  7 - Obtener el top 10 de libros más descargados          **
                    **                                                           **
                    **  0 - Salir                                                **
                    ********                                               ********
                    """;

            System.out.println(menu);
            opcion = read.nextInt();
            read.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    listarBuscados();
                    break;
                case 3:
                    listarAutores();
                    break;
                case 4:
                    listarAutoresVivos();
                    break;
                case 5:
                    listarLibrosIdioma();
                    break;
                case 6:
                    obtenerEstadisticas();
                    break;
                case 7:
                    obtenerMasDescargados();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    private void buscarLibro() {
        Datos datos = obtenerDatosLibro();

        Optional<DatosLibro> libros = datos.resultados().stream()
                .findFirst();

        if (libros.isPresent()) {
            Libro libro = new Libro(libros.get());
            Autor autor = new Autor(libros.get().autor().get(0));


            if (libroRepositorio.existsByTitulo(libro.getTitulo())){
                System.out.println("Este libro ya se encuentra en la base de datos");

            } else {
                autorEncontrado = autorRepositorio.findByNombreContainsIgnoreCase(autor.getNombre());
                if (autorEncontrado.isPresent()) {
                    var autorIngresado = autorEncontrado.get();
                    libro.setAutor(autorIngresado);
                    libroRepositorio.save(libro);
                } else {
                    autor.setLibrosDelAutor(libro);
                    autorRepositorio.save(autor);
                }
                System.out.println(libro.toString());
                System.out.println("Libro registrado en la base de datos!");
            }
        } else {
            System.out.println("Libro no encontrado!");
        }
    }

    public Datos obtenerDatosLibro() {
        System.out.println("Ingresa el nombre del libro que deseas buscar:");
        String nombreDelLibro = read.nextLine().toLowerCase().replace(" ", "%20");
        var json = consumoApi.obtenerDatos(URL_BASE + nombreDelLibro);
        var datos = conversor.obtenerDatos(json, Datos.class);
        return datos;
    }

    public void listarBuscados() {
        System.out.println("Lista de libros buscados");
        libros = libroRepositorio.findAll();
        libros.stream()
                .forEach(System.out::println);
    }

    public void listarAutores() {
        autores = autorRepositorio.findAll();
        autores.stream()
                .forEach(System.out::println);
    }

    public void listarAutoresVivos() {
        System.out.println("Ingresa el año para la búsqueda de autores");
        var fechaReferencia = read.nextInt();
        read.nextLine();
        autores = autorRepositorio.findAll();
        autores.stream()
                .filter(a -> (fechaReferencia <= a.getFechaDeDeceso())&&(fechaReferencia >=a.getFechaDeNacimiento()))
                .forEach(System.out::println);
    }

    public void listarLibrosIdioma() {
        System.out.println("""
                Ingresa el idioma del cual quieres obtener la lista
                Español     ---> (es)
                Ingles      ---> (en)
                Frances     ---> (fr)
                Portugués   ---> (pt)                
                """);
        final var idioma = read.nextLine().toLowerCase();
        System.out.println(idioma);
        if (Arrays.asList(idiomas).contains(idioma)) {
            libros = libroRepositorio.findAll();
            libros.stream()
                    .filter(l -> l.getIdioma().equals(idioma))
                    .forEach(System.out::println);
        } else {
            System.out.println("La opción que has ingresado no es valida!");
        }
    }

    public void obtenerEstadisticas() {
        libros = libroRepositorio.findAll();
        DoubleSummaryStatistics est = libros.stream()
                .filter(d -> d.getCantidadDescargas() > 0)
                .collect(Collectors.summarizingDouble(Libro::getCantidadDescargas));

        System.out.println("\nEstadisticas obtenidas de la base de datos");
        System.out.println("Cantidad media de descargas: " + est.getAverage());
        System.out.println("Cantidad máxima de descargas: " + est.getMax());
        System.out.println("Cantidad mínima de descargas: " + est.getMin());
        System.out.println("Cantidad de registros evaluados: " + est.getCount());
    }

    public void obtenerMasDescargados() {
        System.out.println("\nLibros más descargados");
        libros = libroRepositorio.findAll();
        libros.stream()
                .sorted(Comparator.comparing(Libro::getCantidadDescargas).reversed())
                .limit(10)
                .map(l -> l.getTitulo().toUpperCase())
                .forEach(System.out::println);
    }
}