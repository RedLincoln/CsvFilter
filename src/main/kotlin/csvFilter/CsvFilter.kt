package csvFilter

class CsvFilter {
    private val percentage: Double = 1.0/100
    private var ids : Map<String, Int> = mapOf()

    fun apply(lines: List<String>) : List<String> {
        val noHeader = lines.size <= 1
        if (noHeader){
            return listOf("")
        }
        var result = mutableListOf<String>()
        result.add(lines[0])
        val invoices = lines.slice(IntRange(1, lines.size - 1))
        ids = invoices.groupingBy { it.split(',')[0] }.eachCount()
        val goodInvoices = invoices.filter { invoice -> lineFilter(invoice) }
        result.addAll(goodInvoices)
        return result.toList()
    }

    private fun lineFilter(line : String):Boolean{
        val fields = line.split(',')
        val idFieldIndex = 0
        val idField = fields[idFieldIndex]
        if (line.isNullOrEmpty() || (ids[idField]!! > 1)){return false}
        val ivaFieldIndex = 4
        val igicFieldIndex = 5
        val grossAmountIndex = 2
        val netAmountIndex = 3
        val cifFieldIndex = 7
        val nifFieldIndex = 8
        val grossField = fields[grossAmountIndex]
        val netField = fields[netAmountIndex]
        val ivaField = fields[ivaFieldIndex]
        val igicField = fields[igicFieldIndex]
        val cifField = fields[cifFieldIndex]
        val nifField = fields[nifFieldIndex]
        val decimalRegex = "\\d+(\\.\\d+)?".toRegex()
        val taxFieldsAreMutuallyExclusive =
            (ivaField.matches(decimalRegex) || igicField.matches(decimalRegex)) &&
                    (ivaField.isNullOrEmpty() || igicField.isNullOrEmpty())
        val idFieldsAreMutuallyExclusive = (cifField.isNullOrEmpty() xor nifField.isNullOrEmpty())
        if (taxFieldsAreMutuallyExclusive && idFieldsAreMutuallyExclusive) {
            val tax : Double = if (ivaField.isNullOrEmpty()) igicField.toDouble() else ivaField.toDouble()
            if (grossField.toDouble() - grossField.toDouble()*tax*percentage == netField.toDouble()) {
                return true
            }
        }
        return false
    }
}
