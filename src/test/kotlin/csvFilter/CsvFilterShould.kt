package csvFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CsvFilterShould {
    private val headerLine = "Num_factura, Fecha, Bruto, Neto, IVA, IGIC, Concepto, CIF_cliente, NIF_cliente"
    lateinit var filter: CsvFilter
    private val emptyDataFile = Csv.Builder().build().toList() // listOf(headerLine)
    private val emptyField = ""

    @Before
    fun setup() {
        filter = CsvFilter()
    }

    @Test
    fun correct_lines_are_not_filtered() {
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().concept("a correct line with irrelevant data").build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(lines)
    }

    @Test
    fun tax_fields_are_mutually_exclusive() {
        var lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax("19").igicTax("8").build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun there_must_be_at_least_one_tax_for_the_invoice() {
        var lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax(emptyField).igicTax(emptyField).build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun tax_fields_must_be_decimals() {
        var lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax("XYZ").igicTax(emptyField).build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun tax_fields_must_be_decimals_and_exclusive_igic_decimal() {
        var lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax("XYZ").igicTax("19").build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun tax_fields_must_be_decimals_and_exclusive_iva_decimal() {
        var lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax("19").igicTax("XYZ").build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun net_amount_have_to_be_well_calculated_iva_good() {
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax("19").igicTax(emptyField).build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(lines)
    }

    @Test
    fun net_amount_have_to_be_well_calculated_iva_bad() {
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax("9").igicTax(emptyField).build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun net_amount_have_to_be_well_calculated_igic_good() {
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax(emptyField).igicTax("19").build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(lines)
    }

    @Test
    fun net_amount_have_to_be_well_calculated_igic_bad() {
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().ivaTax(emptyField).igicTax("9").build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun id_fields_are_mutually_exclusive() {
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().cif("B76430134").nif("12345678Z").build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun id_fields_are_mutually_exclusive_nif_set() {
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().cif(emptyField).nif("12345678Z").build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(lines)
    }

    @Test
    fun have_two_or_more_lines() {
        val lines = Csv.Builder().build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(listOf(emptyField))
    }

    @Test
    fun tax_fields_must_be_decimals_and_exclusive_multiple_lines() {
        val goodLine = CsvInvoice.Builder().invoiceId("2").ivaTax("19").igicTax(emptyField).build()
        val lines = Csv.Builder().appendAll(
            CsvInvoice.Builder().ivaTax("19").igicTax("XYZ").build(),
            goodLine
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(listOf(headerLine, goodLine.toString()))
    }

    @Test
    fun lines_with_same_id_must_be_eliminated() {
        val lines = Csv.Builder().appendAll(
            CsvInvoice.Builder().invoiceId("1").ivaTax("19").igicTax("XYZ").build(),
            CsvInvoice.Builder().invoiceId("1").ivaTax("19").igicTax(emptyField).build()
        ).build().toList()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun empty_input_must_output_empty_list() {
        val lines = listOf<String>()

        val result = filter.apply(lines)

        assertThat(result).isEqualTo(listOf(emptyField))
    }

    @Test
    fun gross_must_be_decimal(){
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().grossAmount("XYZ").build()
        ).build().toList()

        val result =  filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun net_must_be_decimal(){
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().netAmount("XYZ").build()
        ).build().toList()

        val result =  filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun cif_number_must_be_valid(){
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().cif("XYZ").nif(emptyField).build()
        ).build().toList()

        val result =  filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }

    @Test
    fun nif_number_must_be_valid(){
        val lines = Csv.Builder().appendLine(
            CsvInvoice.Builder().nif("XYZ").cif(emptyField).build()
        ).build().toList()

        val result =  filter.apply(lines)

        assertThat(result).isEqualTo(emptyDataFile)
    }


}