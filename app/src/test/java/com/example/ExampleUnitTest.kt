package com.example

import com.example.data.IndicatorType
import com.example.data.remote.GitHubFileKey
import com.example.data.remote.HostingerConfig
import com.example.data.remote.HostingerScriptGenerator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify the 5 github excel file names and indicators mapping`() {
    val fileNames = GitHubFileKey.values().map { it.fileNameBase }
    assertTrue(fileNames.contains("tecnico_certificao"))
    assertTrue(fileNames.contains("revisita30d"))
    assertTrue(fileNames.contains("certidao_atendimento"))
    assertTrue(fileNames.contains("cop_360"))
    assertTrue(fileNames.contains("tnps"))
    assertEquals(5, fileNames.size)

    assertEquals(IndicatorType.TECNICO_CERTIFICADO, GitHubFileKey.TECNICO_CERTIFICADO.indicatorType)
    assertEquals(IndicatorType.REVISITA_30D, GitHubFileKey.REVISITA_30D.indicatorType)
    assertEquals(IndicatorType.CERTIDAO_ATENDIMENTO, GitHubFileKey.CERTIDAO_ATENDIMENTO.indicatorType)
    assertEquals(IndicatorType.COP_360, GitHubFileKey.COP_360.indicatorType)
    assertEquals(IndicatorType.TNPS, GitHubFileKey.TNPS.indicatorType)
  }

  @Test
  fun `verify hostinger php script and schema generation`() {
    val config = HostingerConfig(
      apiUrl = "https://example.com/api/indicadores_api.php",
      apiKey = "secret_key_123"
    )
    val php = HostingerScriptGenerator.getPhpScript(config)
    assertTrue(php.contains("secret_key_123"))
    assertTrue(php.contains("indicator_records"))
    assertTrue(php.contains("CREATE TABLE IF NOT EXISTS"))

    val sql = HostingerScriptGenerator.getSqlSchema()
    assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `indicator_records`"))
  }
}
