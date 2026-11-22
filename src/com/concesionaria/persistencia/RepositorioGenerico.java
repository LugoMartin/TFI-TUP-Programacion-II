package com.concesionaria.persistencia;

import java.io.IOException;
import java.util.List;

import com.concesionaria.modelo.Vehiculo;
import com.concesionaria.modelo.excepciones.DatoInvalidoException;
import com.concesionaria.modelo.excepciones.VehiculoDuplicadoException;
import com.concesionaria.modelo.excepciones.VehiculoNoEncontradoException;

/**
 * Contrato generico de persistencia para entidades derivadas de {@link Vehiculo}.
 * <p>
 * Expone las operaciones CRUD basicas y mantiene desacoplada la logica de negocio
 * del mecanismo concreto de almacenamiento (archivo, base de datos, memoria, etc.).
 * </p>
 *
 * @param <T>  tipo de entidad persistida, siempre una subclase de {@link Vehiculo}
 * @param <ID> tipo del identificador unico de la entidad
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public interface RepositorioGenerico<T extends Vehiculo, ID> {

    /**
     * Registra una entidad nueva en el repositorio.
     *
     * @param entidad entidad a persistir
     * @throws IOException                si no puede escribirse el soporte de datos
     * @throws DatoInvalidoException      si la entidad es nula o tiene datos incompletos
     * @throws VehiculoDuplicadoException si ya existe una entidad con el mismo identificador
     */
    void guardar(T entidad) throws IOException, DatoInvalidoException, VehiculoDuplicadoException;

    /**
     * Busca una entidad por su identificador unico.
     *
     * @param id identificador de la entidad buscada
     * @return la entidad encontrada
     * @throws IOException                  si no puede leerse el soporte de datos
     * @throws DatoInvalidoException        si el contenido persistido es invalido
     * @throws VehiculoNoEncontradoException si no existe una entidad con ese identificador
     */
    T buscarPorId(ID id) throws IOException, DatoInvalidoException, VehiculoNoEncontradoException;

    /**
     * Devuelve todas las entidades registradas.
     *
     * @return lista con las entidades persistidas; vacia si el repositorio esta vacio
     * @throws IOException           si no puede leerse el soporte de datos
     * @throws DatoInvalidoException si el contenido persistido es invalido
     */
    List<T> listarTodos() throws IOException, DatoInvalidoException;

    /**
     * Actualiza una entidad existente identificada por su id.
     *
     * @param entidad entidad con los datos ya modificados
     * @throws IOException                  si no puede escribirse el soporte de datos
     * @throws DatoInvalidoException        si la entidad es nula o tiene datos incompletos
     * @throws VehiculoNoEncontradoException si no existe una entidad con ese identificador
     */
    void actualizar(T entidad) throws IOException, DatoInvalidoException, VehiculoNoEncontradoException;

    /**
     * Elimina la entidad que coincide con el identificador indicado.
     *
     * @param id identificador de la entidad a borrar
     * @throws IOException                  si no puede escribirse el soporte de datos
     * @throws DatoInvalidoException        si el contenido persistido es invalido
     * @throws VehiculoNoEncontradoException si no existe una entidad con ese identificador
     */
    void eliminar(ID id) throws IOException, DatoInvalidoException, VehiculoNoEncontradoException;

    /**
     * Comprueba si existe una entidad con el identificador indicado.
     *
     * @param id identificador a verificar
     * @return {@code true} si el identificador ya esta registrado
     * @throws IOException           si no puede leerse el soporte de datos
     * @throws DatoInvalidoException si el contenido persistido es invalido
     */
    boolean existe(ID id) throws IOException, DatoInvalidoException;
}
