package com.uncmorfi.data.network

import org.junit.Test

class MenuParserTest {

    @Test
    fun `se puede parsear el menu de la semana directamente`() {
        val out = MenuParser.fetch()
        println(out)
    }
}