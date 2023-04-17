package com.uncmorfi.data.repository

import com.uncmorfi.data.network.models.Menu
import com.uncmorfi.shared.DateUtils.FORMAT_ARG1
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList

class RepoMenu {

    private val URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana"

    fun fetch(): Menu {
        val doc: Document = Jsoup.connect(URL).get()

        // Seleccionar la parte del menú
        val menu: Element = doc.select("div[property=content:encoded]").first()!!

        val result = mutableMapOf<Calendar, MutableList<String>>()
        var current = Calendar.getInstance()
        var month = "Abril 2023 "

        for (child in menu.children()) {
            if (child.childrenSize() == 0) continue

            if (child.normalName() == "p") {
                if (child.childrenSize() == 1) {
                    val first = child.child(0)
                    val cal = Calendar.getInstance()
                    cal.time = Date.from(
                        LocalDate.parse(month + first.text(), FORMAT_ARG1).atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                    )
                    current = cal
                    result[current] = mutableListOf()
                    continue
                }

                val food = child.html()
                    .split("<br>")
                    .map { Jsoup.clean(it, Safelist.none()) }
                    .map { Parser.unescapeEntities(it, false) }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                result[current]?.addAll(food)

            } else if (child.normalName() == "ul") {
                val food = child
                    .getElementsByTag("li")
                    .map { it.text() }

                result[current]!!.addAll(food)
            }
        }

        return Menu(result)
    }


    fun splitElementBy(element: Element, fn: ((Element) -> Boolean)): List<List<Element>> {
        return element.children().fold(ArrayList<ArrayList<Element>>()) { list, item ->
            list.apply {
                when {
                    isEmpty() -> add(arrayListOf(item))
                    fn(item) -> add(arrayListOf())
                    else -> last().add(item)
                }
            }
        }
    }
}