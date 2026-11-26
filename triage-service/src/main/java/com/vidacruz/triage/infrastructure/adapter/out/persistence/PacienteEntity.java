package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("pacientes")
public class PacienteEntity {
    @Id
    private String id;

    @Column("nombre_completo")
    private String nombreCompleto;

    @Column("edad")
    private Integer edad;

    @Column("frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column("presion_sistolica")
    private Integer presionSistolica;

    @Column("presion_diastolica")
    private Integer presionDiastolica;

    @Column("spo2")
    private Integer spo2;

    @Column("temperatura")
    private Double temperatura;

    @Column("dolor")
    private Integer dolor;

    @Column("gcs")
    private Integer gcs;

    @Column("queja_principal")
    private String quejaPrincipal;

    @Column("nivel_triaje")
    private String nivelTriaje;

    @Column("estado")
    private String estado;

    @Column("escalado")
    private Boolean escalado;

    @Column("fecha_registro")
    private Instant fechaRegistro;

    public PacienteEntity() {
    }

    // Getters y setters (necesarios para R2DBC)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public Integer getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }

    public Integer getPresionSistolica() { return presionSistolica; }
    public void setPresionSistolica(Integer presionSistolica) { this.presionSistolica = presionSistolica; }

    public Integer getPresionDiastolica() { return presionDiastolica; }
    public void setPresionDiastolica(Integer presionDiastolica) { this.presionDiastolica = presionDiastolica; }

    public Integer getSpo2() { return spo2; }
    public void setSpo2(Integer spo2) { this.spo2 = spo2; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Integer getDolor() { return dolor; }
    public void setDolor(Integer dolor) { this.dolor = dolor; }

    public Integer getGcs() { return gcs; }
    public void setGcs(Integer gcs) { this.gcs = gcs; }

    public String getQuejaPrincipal() { return quejaPrincipal; }
    public void setQuejaPrincipal(String quejaPrincipal) { this.quejaPrincipal = quejaPrincipal; }

    public String getNivelTriaje() { return nivelTriaje; }
    public void setNivelTriaje(String nivelTriaje) { this.nivelTriaje = nivelTriaje; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Boolean getEscalado() { return escalado; }
    public void setEscalado(Boolean escalado) { this.escalado = escalado; }

    public Instant getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
