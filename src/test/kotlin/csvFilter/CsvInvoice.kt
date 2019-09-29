package csvFilter

class CsvInvoice(
    var invoiceId : String,
    var invoiceDate : String,
    var grossAmount : String,
    var netAmount : String,
    var ivaTax : String,
    var igicTax : String,
    var concept : String,
    var cif : String,
    var nif : String) {

    data class Builder(
        var invoiceId: String = "1",
        var invoiceDate: String = "02/05/2019",
        var grossAmount: String = "1000",
        var netAmount: String = "810",
        var ivaTax: String = "19",
        var igicTax: String = "",
        var concept: String = "irrelevant",
        var cif: String = "B76430134",
        var nif: String = "") {

        fun invoiceId(invoiceId: String) = apply { this.invoiceId = invoiceId }
        fun invoiceDate(invoiceDate: String) = apply { this.invoiceDate = invoiceDate }
        fun grossAmount(grossAmount: String) = apply { this.grossAmount = grossAmount }
        fun netAmount(netAmount: String) = apply { this.netAmount = netAmount }
        fun ivaTax(ivaTax: String) = apply { this.ivaTax = ivaTax }
        fun igicTax(igicTax: String) = apply { this.igicTax = igicTax }
        fun concept(concept: String) = apply { this.concept = concept }
        fun cif(cif: String) = apply { this.cif = cif }
        fun nif(nif: String) = apply { this.nif = nif }
        fun build() = CsvInvoice(invoiceId, invoiceDate, grossAmount, netAmount, ivaTax, igicTax, concept, cif, nif)

    }


    override fun toString (): String {
        return listOf(
            invoiceId,
            invoiceDate,
            grossAmount,
            netAmount,
            ivaTax,
            igicTax,
            concept,
            cif,
            nif
        ).joinToString(",")
    }
}