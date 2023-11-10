package com.uncmorfi.data.network

import com.uncmorfi.shared.PROFILE_PIC_URL
import io.mockk.mockk
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class UserParserTest {

    private val estudiante =
        "google.visualization.Query.setResponse({status:'ok', table: {cols:[{id: 'c0', label: 'id_cliente', type:'number'}, {id: 'c1', label: 'beca', type:'number'}, {id: 'c2', label: 'renovado', type:'date'}, {id: 'c3', label: 'fecha_desde', type:'date'}, {id: 'c4', label: 'fecha_hasta', type:'date'}, {id: 'c5', label: 'saldo', type:'number'}, {id: 'c6', label: 'raciones', type:'number'}, {id: 'c7', label: 'sede', type:'string'}, {id: 'c8', label: 'tipo_cliente', type:'string'}, {id: 'c9', label: 'tipo_beca', type:'string'}, {id: 'c10', label: 'tipo_monto', type:'string'}, {id: 'c11', label: 'beca_tc', type:'number'}, {id: 'c12', label: 'tipo_duracion', type:'string'}, {id: 'c13', label: 'tipo_renovacion', type:'string'}, {id: 'c14', label: 'max_personas_agregar', type:'number'}, {id: 'c15', label: 'renovacion', type:'string'}, {id: 'c16', label: 'nombre', type:'string'}, {id: 'c17', label: 'apellido', type:'string'}, {id: 'c18', label: 'cuip', type:'number'}, {id: 'c19', label: 'otro', type:'string'}, {id: 'c20', label: 'mail', type:'string'}, {id: 'c21', label: 'evento', type:'string'}, {id: 'c22', label: 'precio_x_racion', type:'number'}, {id: 'c23', label: 'prox_renov', type:'date'}, {id: 'c24', label: 'foto', type:'string'}, {id: 'c25', label: 'codigo', type:'string'}], rows: [{c: [{v: 1},{v: null},{v: new Date(2023, 2, 20)},{v: new Date(2016, 2, 10)},{v: new Date(2024, 2, 20)},{v: 3.29998},{v: null},{v: '0475'},{v: 'Estudiante de Grado'},{v: 'D'},{v: '\$'},{v: 452.31},{v: 'S'},{v: 'F'},{v: 0},{v: null},{v: 'Nombre'},{v: 'Apellido'},{v: 20000000003},{v: null},{v: 'estudiante@mi.unc.edu.ar'},{v: null},{v: 800.65},{v: null},{v: 'estudiante_pic_url'},{v: '0475ESTUDIANTE1'}]}]}});"
    private val becado =
        "google.visualization.Query.setResponse({status:'ok', table: {cols:[{id: 'c0', label: 'id_cliente', type:'number'}, {id: 'c1', label: 'beca', type:'number'}, {id: 'c2', label: 'renovado', type:'date'}, {id: 'c3', label: 'fecha_desde', type:'date'}, {id: 'c4', label: 'fecha_hasta', type:'date'}, {id: 'c5', label: 'saldo', type:'number'}, {id: 'c6', label: 'raciones', type:'number'}, {id: 'c7', label: 'sede', type:'string'}, {id: 'c8', label: 'tipo_cliente', type:'string'}, {id: 'c9', label: 'tipo_beca', type:'string'}, {id: 'c10', label: 'tipo_monto', type:'string'}, {id: 'c11', label: 'beca_tc', type:'number'}, {id: 'c12', label: 'tipo_duracion', type:'string'}, {id: 'c13', label: 'tipo_renovacion', type:'string'}, {id: 'c14', label: 'max_personas_agregar', type:'number'}, {id: 'c15', label: 'renovacion', type:'string'}, {id: 'c16', label: 'nombre', type:'string'}, {id: 'c17', label: 'apellido', type:'string'}, {id: 'c18', label: 'cuip', type:'number'}, {id: 'c19', label: 'otro', type:'string'}, {id: 'c20', label: 'mail', type:'string'}, {id: 'c21', label: 'evento', type:'string'}, {id: 'c22', label: 'precio_x_racion', type:'number'}, {id: 'c23', label: 'prox_renov', type:'date'}, {id: 'c24', label: 'foto', type:'string'}, {id: 'c25', label: 'codigo', type:'string'}], rows: [{c: [{v: 1},{v: null},{v: new Date(2023, 4, 9)},{v: new Date(2020, 6, 21)},{v: new Date(2024, 4, 9)},{v: null},{v: 158},{v: '0475'},{v: 'BECAS NUTRIRSE 2023 VIANDA'},{v: 'C'},{v: null},{v: null},{v: 'F'},{v: 'F'},{v: 2},{v: null},{v: 'Nombre'},{v: 'Apellido'},{v: 20000000003},{v: '35963286'},{v: 'becado@mi.unc.edu.ar'},{v: null},{v: 800.65},{v: null},{v: 'becado_pic_url'},{v: '0475BECADO12345'}]}]}});"

    private val okHttpClient : OkHttpClient = mockk()

    private lateinit var userParser: UserParser

    @Before
    fun signUp(){
        userParser = UserParser(okHttpClient)
    }

    @Test
    fun `se puede parsear los datos de un estudiante`() {
        val out = userParser.parse(estudiante)

        assertNotNull(out)
        assertEquals(out!!.card, "0475ESTUDIANTE1")
        assertEquals(out.name, "Nombre")
        assertEquals(out.type, "Estudiante de Grado")
        assertEquals(out.email, "estudiante@mi.unc.edu.ar")
        assertEquals(out.image, PROFILE_PIC_URL + "estudiante_pic_url")
        assertEquals(out.balance, BigDecimal("3.29998"))
        assertEquals(out.price, BigDecimal("348.34"))
        assertEquals(out.expiration, LocalDate.of(2024, 3, 20))
    }

    @Test
    fun `se puede parsear los datos de un becado`() {
        val out = userParser.parse(becado)

        assertNotNull(out)
        assertEquals(out!!.card, "0475BECADO12345")
        assertEquals(out.name, "Nombre")
        assertEquals(out.type, "BECAS NUTRIRSE 2023 VIANDA")
        assertEquals(out.email, "becado@mi.unc.edu.ar")
        assertEquals(out.image, PROFILE_PIC_URL + "becado_pic_url")
        assertEquals(out.balance, null)
        assertEquals(out.rations, 158)
        assertEquals(out.expiration, LocalDate.of(2024, 5, 9))
    }

    @Test
    fun `se puede parsear la fecha`() {
        val input = "new Date(2024, 2, 20)"
        val out = userParser.parseExpiration(input)
        assertEquals(out, LocalDate.of(2024, 3, 20))
    }

    @Test
    fun `se puede parsear otra fecha`() {
        val input = "new Date(2024, 2, 2)"
        val out = userParser.parseExpiration(input)
        assertEquals(out, LocalDate.of(2024, 3, 2))
    }
}