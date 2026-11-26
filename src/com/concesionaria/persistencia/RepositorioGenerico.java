package com.concesionaria.persistencia;

import java.io.IOException;
import java.util.List;

import com.concesionaria.modelo.Vehiculo;
import com.concesionaria.modelo.excepciones.DatoInvalidoException;
import com.concesionaria.modelo.excepciones.VehiculoDuplicadoException;
import com.concesionaria.modelo.excepciones.VehiculoNoEncontradoException;

public interface RepositorioGenerico<T extends Vehiculo, ID> {

    void guardar(T entidad) throws IOException, DatoInvalidoException, VehiculoDuplicadoException;

    T buscarPorId(ID id) throws IOException, DatoInvalidoException, VehiculoNoEncontradoException;

    List<T> listarTodos() throws IOException, DatoInvalidoException;

    void actualizar(T entidad) throws IOException, DatoInvalidoException, VehiculoNoEncontradoException;

    void eliminar(ID id) throws IOException, DatoInvalidoException, VehiculoNoEncontradoException;

    boolean existe(ID id) throws IOException, DatoInvalidoException;
}
