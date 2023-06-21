package com.uncmorfi.data.network

import com.uncmorfi.ui.servings.HourAxisValueFormatter
import junit.framework.Assert.assertEquals
import org.junit.Test

class ServingParserTest {

    //cols: \[(.*)\], rows: \[(.*)\s\]\s}\s}$

    private val input = """
        google.visualization.Query.setResponse(
        {
            reqId: 0,
            status:'ok',
            table: {
                cols: [
                    {id: 'c0', label: 'fecha', type:'string'},
                    {id: 'c1', label: 'raciones', type:'number'}
                ], rows: [
                    {c: [
                        {v: '12:04:00'},
                        {v: 12}
                    ]},
                    {c: [{v: '12:05:00'},{v: 6}]},
                    {c: [{v: '12:06:00'},{v: 6}]},
                    {c: [{v: '12:07:00'},{v: 14}]},
                    {c: [{v: '12:08:00'},{v: 10}]},
                    {c: [{v: '12:09:00'},{v: 13}]},
                    {c: [{v: '12:10:00'},{v: 11}]},
                    {c: [{v: '12:11:00'},{v: 10}]},
                    {c: [{v: '12:12:00'},{v: 7}]},
                    {c: [{v: '12:13:00'},{v: 8}]},
                    {c: [{v: '12:14:00'},{v: 9}]},
                    {c: [{v: '12:15:00'},{v: 5}]},
                    {c: [{v: '12:16:00'},{v: 13}]},
                    {c: [{v: '12:17:00'},{v: 8}]},
                    {c: [{v: '12:18:00'},{v: 4}]},
                    {c: [{v: '12:19:00'},{v: 12}]},
                    {c: [{v: '12:20:00'},{v: 10}]},
                    {c: [{v: '12:21:00'},{v: 12}]},
                    {c: [{v: '12:22:00'},{v: 11}]}
                ]
            }
        });
    """.trimIndent()

    val formatter = HourAxisValueFormatter()

    @Test
    fun `se puede parsear los datos de una tarjeta`() {
        val out = ServingParser.parseBody(input)
        println(out)
        val display = formatter.format(out.first().toFloat())
        assertEquals("12:04", display)
    }

}