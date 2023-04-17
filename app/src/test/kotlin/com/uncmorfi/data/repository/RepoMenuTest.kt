package com.uncmorfi.data.repository

import org.junit.Test
import org.jsoup.Jsoup
import org.junit.Assert


class RepoMenuTest {

    private val repoMenu = RepoMenu()

    @Test
    fun `se puede parsear el menu de la semana directamente`() {
        val out = repoMenu.fetch()
        println(out)
    }

    @Test
    fun `se puede hacer split`() {
        val html = "<p><strong>Vegetariano:</strong>lomo vegetariano + ensalada completa<br>" +
                "<strong>Sin TACC:</strong> milanesa con jardinera + fruta</p>"
        val doc = Jsoup.parse(html).getElementsByTag("p").first()!!

        val out = repoMenu.splitElementBy(doc) { it.normalName() == "br" }

        println(out)

        Assert.assertEquals(out.size, 2)
    }

}