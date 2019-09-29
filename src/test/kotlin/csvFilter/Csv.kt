package csvFilter

class Csv(val header : String,
          val invoices: List<CsvInvoice>) {

    open class Builder (
        var header : String = "Num_factura, Fecha, Bruto, Neto, IVA, IGIC, Concepto, CIF_cliente, NIF_cliente",
        var invoices : MutableList<CsvInvoice> = mutableListOf()
    ) {
        fun header(header: String) = apply { this.header = header }
        fun appendLine(invoice: CsvInvoice ) = apply { this.invoices.add(invoice) }
        fun build() = Csv(header, invoices)
    }

    fun toList(): List<String> {
        val result = mutableListOf<String>()
        result.add(header)
        invoices.forEach {invoice -> result.add(invoice.toString())}
        return result.toList()
    }
}