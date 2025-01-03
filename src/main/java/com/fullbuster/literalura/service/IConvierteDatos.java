package com.fullbuster.literalura.service;

//CREADA EN EL VIDEO 1.4
public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}

