package com.agrovalleconnect.agrovalle_connect.repository;

import com.agrovalleconnect.agrovalle_connect.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
            select p from Producto p
            join fetch p.finca f
            join fetch f.municipio m
            join fetch p.categoria c
            where p.estado = ACTIVO and p.cantidadDisponible > 0
              and (trim(coalesce(:municipio, '')) = '' or lower(m.nombre) = lower(trim(:municipio)))
              and (trim(coalesce(:categoria, '')) = '' or lower(c.nombre) = lower(trim(:categoria)))
            order by p.id asc
            """)
    List<Producto> consultarOfertasDisponibles(@Param("municipio") String municipio,
                                               @Param("categoria") String categoria);

    /**
     * HU-07: carga los productos con bloqueo de escritura para restituir stock sin
     * perder actualizaciones concurrentes. El orden por id evita interbloqueos
     * entre transacciones que bloquean los mismos productos.
     *
     * @param ids identificadores de los productos
     * @return los productos encontrados, ordenados por id
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id in :ids order by p.id asc")
    List<Producto> buscarParaActualizar(@Param("ids") Collection<Long> ids);
}
