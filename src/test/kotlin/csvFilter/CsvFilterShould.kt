package csvFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CsvFilterShould {
    private val headerLine = "Num_factura, Fecha, Bruto, Neto, IVA, IGIC, Concepto, CIF_cliente, NIF_cliente"
    lateinit var filter : CsvFilter
    private val emptyDataFile = listOf(headerLine)
    private val emptyField = ""

    @Before
    fun setup(){
        filter = CsvFilter()
    }

    @Test
    fun correct_lines_are_not_filtered(){
        val lines = fileWithOneInvoiceLineHaving(concept = "a correct line with irrelevant data")
        val result = filter.apply(lines)

        assertThat(result).isEqualTo(lines)
    }

    @Test
    fun tax_fields_are_mutually_exclusive(){
        val result = filter.apply(fileWithOneInvoiceLineHaving(ivaTax = "19", igicTax = "8"))

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun there_must_be_at_least_one_tax_for_the_invoice(){
        val result = filter.apply(fileWithOneInvoiceLineHaving(ivaTax = emptyField, igicTax = emptyField))

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun tax_fields_must_be_decimals(){
        val result = filter.apply(fileWithOneInvoiceLineHaving(ivaTax = "XYZ", igicTax = emptyField))

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun tax_fields_must_be_decimals_and_exclusive_igic_decimal(){
        val result = filter.apply(fileWithOneInvoiceLineHaving(ivaTax = "XYZ", igicTax = "19"))

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun tax_fields_must_be_decimals_and_exclusive_iva_decimal(){
        val result = filter.apply(fileWithOneInvoiceLineHaving(ivaTax = "19", igicTax = "XYZ"))

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun net_amount_have_to_be_well_calculated_iva_good(){
        val lines = fileWithOneInvoiceLineHaving(ivaTax = "19", igicTax = emptyField)
        val result = filter.apply(lines)

        assertThat(result).isEqualTo(lines)
    }

    @Test
    fun net_amount_have_to_be_well_calculated_iva_bad(){
        val lines = fileWithOneInvoiceLineHaving(ivaTax = "9", igicTax = emptyField)
        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun net_amount_have_to_be_well_calculated_igic_good(){
        val lines = fileWithOneInvoiceLineHaving(ivaTax = emptyField, igicTax = "19")
        val result = filter.apply(lines)

        assertThat(result).isEqualTo(lines)
    }

    @Test
    fun net_amount_have_to_be_well_calculated_igic_bad(){
        val lines = fileWithOneInvoiceLineHaving(ivaTax = emptyField, igicTax = "9")
        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun id_fields_are_mutually_exclusive(){
        val result = filter.apply(fileWithOneInvoiceLineHaving(cif = "B76430134", nif = "X7225252Y"))

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun id_fields_are_mutually_exclusive_nif_set(){
        val lines = fileWithOneInvoiceLineHaving(cif = emptyField, nif = "X7225252Y")
        val result = filter.apply(lines)

        assertThat(result).isEqualTo(lines)
    }

    @Test
    fun file_must_have_a_header(){
        val result = filter.apply(listOf(fileWithOneInvoiceLineHaving()[1]))

        assertThat(result).isEqualTo(listOf(emptyField))
    }

    @Test
    fun tax_fields_must_be_decimals_and_exclusive_multiple_lines(){
        val lines = fileWithOneInvoiceLineHaving(ivaTax = "19", igicTax = "XYZ").toMutableList()
        val goodLine = oneInvoiceLine(invoiceId = "2", ivaTax = "19", igicTax = emptyField)
        lines.add(goodLine)
        val result = filter.apply(lines.toList())


        assertThat(result).isEqualTo(listOf(lines[0], goodLine))
    }

    @Test
    fun lines_with_same_id_must_be_eliminated(){
        val lines = fileWithOneInvoiceLineHaving(invoiceId = "1",ivaTax = "19", igicTax = "XYZ").toMutableList()
        val goodLine = oneInvoiceLine(invoiceId = "1", ivaTax = "19", igicTax = emptyField)
        val anotherLine = oneInvoiceLine(invoiceId = "1", ivaTax = "19", igicTax = emptyField)
        lines.add(goodLine)
        lines.add(anotherLine)
        val result = filter.apply(lines.toList())

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun empty_input_must_output_empty_list(){
        val lines = fileWithOneInvoiceLineHaving(invoiceId = "1",ivaTax = "19", igicTax = "XYZ").toMutableList()
        val goodLine = oneInvoiceLine(invoiceId = "2", ivaTax = "19", igicTax = emptyField)
        lines.add("")
        lines.add(goodLine)
        val result = filter.apply(lines.toList())

        assertThat(result).isEqualTo(listOf(lines[0], lines[3]))
    }

    private fun fileWithOneInvoiceLineHaving(invoiceId : String = "1", nif : String = emptyField,
                                             grossAmount: String = "1000", netAmount: String = "810",
                                             cif : String = "B76430134", ivaTax: String = "19",
                                             igicTax: String = emptyField, concept: String = "irrelevant"): List<String> {
        val invoiceDate = "02/05/2019"
        val formattedLine = listOf(
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
        return listOf(headerLine, formattedLine)
    }

    private fun oneInvoiceLine(invoiceId : String = "1", nif : String = emptyField,
                               grossAmount: String = "1000", netAmount: String = "810",
                               cif : String = "B76430134", ivaTax: String = "19",
                               igicTax: String = emptyField, concept: String = "irrelevant"): String {
        val invoiceDate = "02/05/2019"
        val formattedLine = listOf(
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
        return formattedLine
    }
}
