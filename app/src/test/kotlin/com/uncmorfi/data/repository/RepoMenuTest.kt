package com.uncmorfi.data.repository

import com.uncmorfi.data.network.MenuParser
import org.junit.Test
import org.jsoup.Jsoup
import org.junit.Assert


class RepoMenuTest {

    @Test
    fun `se puede parsear el menu de la semana directamente`() {
        val out = MenuParser.fetch()
        println(out)
    }

}