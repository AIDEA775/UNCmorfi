package com.uncmorfi.data.network

import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.shared.DateUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import java.time.LocalDate

object MenuParser {
    private val URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana"

    fun fetch(): List<DayMenu> {
        val doc: Document = Jsoup.connect(URL).get()

        // Seleccionar la parte del menú
        val menu: Element = doc.select("div[property=content:encoded]").first()!!

        val result = mutableListOf<DayMenu>()
        var currentDay = LocalDate.now()
        val currentFood = mutableListOf<String>()
        var month = "Abril 2023 "

        for (child in menu.children()) {
            if (child.childrenSize() == 0) continue

            if (child.normalName() == "p") {
                // Nuevo día
                if (child.childrenSize() == 1) {
                    val first = child.child(0)
                    // Guardar el resultado
                    if (currentFood.isNotEmpty()) {
                        result.add(DayMenu(currentDay, currentFood.toList()))
                        currentFood.clear()
                    }
                    currentDay = LocalDate.parse(month + first.text(), DateUtils.FORMAT_ARG1)
                    continue
                }

                // Menú vegetariano o sin TACC
                val food = child.html()
                    .split("<br>")
                    .map { Jsoup.clean(it, Safelist.none()) }
                    .map { Parser.unescapeEntities(it, false) }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                currentFood.addAll(food)
            } else if (child.normalName() == "ul") {
                // Menú principal
                val food = child
                    .getElementsByTag("li")
                    .map { it.text() }

                currentFood.addAll(food)
            }
        }

        // Guardar el último resultado
        if (currentFood.isNotEmpty()) {
            result.add(DayMenu(currentDay, currentFood.toList()))
            currentFood.clear()
        }

        return result.toList()
    }

}