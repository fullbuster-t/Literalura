package com.fullbuster.literalura.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    private String nombre;
    private Integer fechaDeNacimiento;
    private Integer fechaDeDeceso;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    //@Transient
    private List<Libro> librosDelAutor;

    public Autor(){

    }

    public Autor(DatosAutor datosDelAutor) {
        this.nombre = datosDelAutor.nombre();
        this.fechaDeNacimiento = Integer.valueOf(datosDelAutor.fechaDeNacimiento());
        this.fechaDeDeceso = Integer.valueOf((datosDelAutor.fechaDeDeceso()));

    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getFechaDeNacimiento() {
        return fechaDeNacimiento;
    }

    public void setFechaDeNacimiento(Integer fechaDeNacimiento) {
        this.fechaDeNacimiento = fechaDeNacimiento;
    }

    public Integer getFechaDeDeceso() {
        return fechaDeDeceso;
    }

    public void setFechaDeDeceso(Integer fechaDeDeceso) {
        this.fechaDeDeceso = fechaDeDeceso;
    }

    public List<String> getLibrosDelAutor() {
        return librosDelAutor.stream()
                .map(libro -> libro.getTitulo())
                .collect(Collectors.toList());
    }

    public void setLibrosDelAutor(Libro libro) {
        librosDelAutor = new ArrayList<>();
        librosDelAutor.add(libro);
        libro.setAutor(this);
    }

    @Override
    public String toString(){
        return
                "Autor = " + nombre +
                        "\nFecha de Nacimiento: " + fechaDeNacimiento +
                        "\nFecha de Fallecimiento: " + fechaDeDeceso +
                        "\nLibros: " + getLibrosDelAutor() +
                        "\n-------------------------------------------";
    }
}
