package csvFilter

class CsvFilter {
    private val percentage: Double = 1.0/100
    private var ids : Map<String, Int> = mapOf()
    private val idFieldIndex = 0
    private val ivaFieldIndex = 4
    private val igicFieldIndex = 5
    private val grossAmountIndex = 2
    private val netAmountIndex = 3
    private val cifFieldIndex = 7
    private val nifFieldIndex = 8

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
        val idField = fields[idFieldIndex]
        if (line.isNullOrEmpty() || ids.getValue(idField) > 1){return false}
        val grossField = fields[grossAmountIndex]
        val netField = fields[netAmountIndex]
        val ivaField = fields[ivaFieldIndex]
        val igicField = fields[igicFieldIndex]
        val cifField = fields[cifFieldIndex]
        val nifField = fields[nifFieldIndex]
        val fieldAreWellFormatted = invoiceHasGoodTypeFormat(fields)
        val taxFieldsAreMutuallyExclusive = ivaField.isNullOrEmpty() xor igicField.isNullOrEmpty()
        val idFieldsAreMutuallyExclusive = cifField.isNullOrEmpty() xor nifField.isNullOrEmpty()
        if (taxFieldsAreMutuallyExclusive && idFieldsAreMutuallyExclusive && fieldAreWellFormatted) {
            val tax : Double = if (ivaField.isNullOrEmpty()) igicField.toDouble() else ivaField.toDouble()
            if (grossField.toDouble() - grossField.toDouble()*tax*percentage == netField.toDouble()) {
                return true
            }
        }
        return false
    }

    private fun invoiceHasGoodTypeFormat(invoice: List<String>): Boolean{
        val decimalRegex = "\\d+(\\.\\d+)?".toRegex()
        val cifRegex = "([A-Z])\\d{8}".toRegex()
        val ivaField = invoice[ivaFieldIndex]
        val igicField = invoice[igicFieldIndex]
        val grossField = invoice[grossAmountIndex]
        val netField = invoice[netAmountIndex]
        val cifField = invoice[cifFieldIndex]
        val nifField =  invoice[nifFieldIndex]

        return (ivaField.matches(decimalRegex) || ivaField.isNullOrEmpty()) &&
                (igicField.matches(decimalRegex) || igicField.isNullOrEmpty()) &&
                (grossField.matches(decimalRegex)) &&
                (netField.matches(decimalRegex)) &&
                (cifField.matches(cifRegex) ||cifField.isNullOrEmpty()) &&
                (nifFieldCheck((nifField)) || nifField.isNullOrEmpty())
    }

    private fun nifFieldCheck(nifField: String ): Boolean{
        val nifReadToBeChecked: String
        val validCheckLetters = "TRWAGMYFPDXBNJZSQVHLCKE"
        val validNieStartLetterS = "XYZ"
        val dniRegex = "\\d{8}[A-Z]".toRegex()
        val nieRegex = ("[$validNieStartLetterS]\\d{7}[$validCheckLetters]").toRegex()
        nifReadToBeChecked = if (nifField.matches(nieRegex)) {
            validCheckLetters.indexOf(nifField[0]).toString() +
                    nifField.substring(1, nifField.lastIndex)
        } else if (nifField.matches(dniRegex)){
            nifField
        } else {
            return false
        }

        val remainder = nifReadToBeChecked.substring(0, nifField.lastIndex ).toInt() % validCheckLetters.length

        if (validCheckLetters[remainder] == nifReadToBeChecked[nifReadToBeChecked.lastIndex]){
            return true
        }
        return false
    }
}

