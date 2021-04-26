import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.collections.ArrayList

data class Question(
    val sector: String,
    val text: String,
    val answerIndex: Int,
    val multipleChoice: List<String>
)

data class RawModel(
    val sector: String,
    val industry: Int,
    val industryName: String,
    val companies: Int,
    val establishments: Int,
    val employees: Int,
    val revenue: Double,
    val payroll: Double,
    val productivity: String,
    val averageSalary: Int,
    val averageHrRate: Double
)

fun main() = runBlocking {
    val rawList = readFile()
    val questions = makeQuestions(rawList)
    val result = Gson().toJson(questions)
    val file = File("questions.json")
    println("size ${questions.size}")
    file.writeText(result)
}

fun readFile(): List<RawModel> {
    val list = ArrayList<RawModel>()
    csvReader().open("US-Industries-Market Sizes.csv") {
        readAllAsSequence().forEachIndexed { index, row: List<String> ->
            if (index != 0) {
                val rawModel = RawModel(
                    sector = row[0],
                    industry = row[1].toInt(),
                    industryName = row[2],
                    companies = row[3].format().toInt(),
                    establishments = row[4].format().toInt(),
                    employees = row[5].format().toInt(),
                    revenue = row[6].format().toDouble(),
                    payroll = row[7].format().toDouble(),
                    productivity = row[8],
                    averageSalary = row[9].format().toInt(),
                    averageHrRate = row[10].toDouble()
                )
                list.add(rawModel)
            }
        }
    }
    return list
}

fun makeQuestions(rawlList: List<RawModel>): List<Question> {
    val prefixList = listOf<String>(
        "How many companies are in the ",
        "How many establishments are in the ",
        "How many employees are in the ",
        "What is the revenue for the ",
        "What is the payroll for the ",
        "What is the productivity for the ",
        "What is the average salary for the ",
        "What is the average hourly rate for the ",
        "What is the sector for the ",
        "The industry code for "
    )
    val questions = arrayListOf<Question>()
    rawlList.forEach { raw ->
        questions.add(makeQuestion(sector = raw.sector, text = prefixList[0] + raw.industryName, answer = raw.companies))
        questions.add(makeQuestion(sector = raw.sector, text = prefixList[1] + raw.industryName, answer = raw.establishments))
        questions.add(makeQuestion(sector = raw.sector, text = prefixList[2] + raw.industryName, answer = raw.employees))
        questions.add(makeQuestion(sector = raw.sector, text = prefixList[3] + raw.industryName, answer = raw.revenue))
        questions.add(makeQuestion(sector = raw.sector, text = prefixList[4] + raw.industryName, answer = raw.payroll))
        questions.add(makeQuestion(sector = raw.sector, text = prefixList[5] + raw.industryName, answer = raw.productivity))
        questions.add(makeQuestion(sector = raw.sector, text = prefixList[6] + raw.industryName, answer = raw.averageSalary))
        questions.add(makeQuestion(sector = raw.sector, text = prefixList[7] + raw.industryName, answer = raw.averageHrRate))
        questions.add(makeSectorQuestion(sector = raw.sector, text = prefixList[8] + raw.industryName))
        questions.add(makeTrueOrFalse(sector = raw.sector, text = prefixList[9] + raw.industryName, industry = raw.industry))
    }
    return questions
}

const val QUESTION_SUFFIX = " industry?"

private fun makeQuestion(sector: String, text: String, answer: Int): Question {
    val list = MutableList(3) { kotlin.random.Random.nextInt(0, answer*2).addCommas() }
    val answerIndex = kotlin.random.Random.nextInt(0, list.size)
    list.add(index = answerIndex, element = answer.addCommas())
    return Question(
        sector = sector,
        text = text + QUESTION_SUFFIX,
        multipleChoice = list,
        answerIndex = answerIndex
    )
}

private fun makeSectorQuestion(sector: String, text: String) : Question {
    val list = MutableList(3) { kotlin.random.Random.nextInt(0, 80).toString() }
    val answerIndex = kotlin.random.Random.nextInt(0, list.size)
    list.add(index = answerIndex, element = sector)
    return Question(
        sector = sector,
        text = text + QUESTION_SUFFIX,
        multipleChoice = list,
        answerIndex = answerIndex
    )
}

private fun makeTrueOrFalse(sector: String, text: String, industry: Int): Question {
    val list = listOf<String>("True", "False")
    val qText = if(kotlin.random.Random.nextBoolean()) {
        text + "is $industry"
    } else {
        text + "is ${kotlin.random.Random.nextInt(0, industry*2)}"
    }
    return Question(
        sector = sector,
        text = qText,
        multipleChoice = list,
        answerIndex = 0
    )
}

private fun makeQuestion(sector: String, text: String, answer: Double): Question {
    val list = MutableList(3) { kotlin.random.Random.nextDouble(0.0, answer*2).addCommas() }
    val answerIndex = kotlin.random.Random.nextInt(0, list.size)
    list.add(index = answerIndex, element = answer.addCommas())
    return Question(
        sector = sector,
        text = text + QUESTION_SUFFIX,
        multipleChoice = list,
        answerIndex = answerIndex
    )
}

private fun makeQuestion(sector: String, text: String, answer: String): Question {
    val list = MutableList(3) { "${kotlin.random.Random.nextInt(0, 3000)}%" }
    val answerIndex = kotlin.random.Random.nextInt(0, list.size)
    list.add(index = answerIndex, element = answer)
    return Question(
        sector = sector,
        text = text + QUESTION_SUFFIX,
        multipleChoice = list,
        answerIndex = answerIndex
    )
}

private fun String.format() = this.replace(",", "")
private fun Int.addCommas() = "%,d".format(this)
private fun Long.addCommas() = "%,d".format(this)
private fun Double.addCommas() = this.toLong().addCommas()