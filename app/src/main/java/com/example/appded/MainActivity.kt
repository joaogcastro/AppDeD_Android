package com.example.appded

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import races.*

class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var createPlayerButton: Button
    private lateinit var selectRaceButton: Button
    private lateinit var selectAbilitiesButton: Button
    private lateinit var playerStatusTextView: TextView
    private var selectedRace: Race? = null
    private val playerBuilder = PlayerBuilder()
    private val player = Player()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameInput = findViewById(R.id.nameInput)
        createPlayerButton = findViewById(R.id.createPlayerButton)
        selectRaceButton = findViewById(R.id.selectRaceButton)
        selectAbilitiesButton = findViewById(R.id.selectAbilitiesButton)
        playerStatusTextView = findViewById(R.id.playerStatusTextView)

        selectRaceButton.setOnClickListener {
            showRaceSelectionDialog()
        }

        selectAbilitiesButton.setOnClickListener {
            showAbilityValueInputDialog()
        }

        createPlayerButton.setOnClickListener {
            val playerName = nameInput.text.toString()
            if (playerName.isEmpty() || selectedRace == null) {
                Toast.makeText(this, "Por favor, insira um nome e selecione uma raça.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            player.name = playerName
            player.race = selectedRace
            playerBuilder.setRaceModifiers(player)
            playerBuilder.setHealthPoints(player)
            val playerStatus = playerBuilder.getStatus(player)
            playerStatusTextView.text = playerStatus
        }
    }

    private fun showRaceSelectionDialog() {
        val races = playerBuilder.getAvailableRaces()
        val raceNames = races.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Selecione uma Raça")
            .setItems(raceNames) { _, which ->
                selectedRace = races[which]
                Toast.makeText(this, "Raça selecionada: ${selectedRace?.name}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAbilityValueInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Insira os Valores das Habilidades")

        val abilities = playerBuilder.abilitiesSample
        val abilityInput = Array(abilities.size) { EditText(this) }
        val remainingPointsTextView = TextView(this)

        // Inicializa os pontos restantes
        var totalPointsSpent = 0

        abilities.forEachIndexed { index, ability ->
            abilityInput[index].hint = "$ability (8-15)"
            abilityInput[index].inputType = android.text.InputType.TYPE_CLASS_NUMBER

            // Listener para atualizar os pontos restantes
            abilityInput[index].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Recalcula os pontos gastos
                    totalPointsSpent = abilityInput.sumOf {
                        it.text.toString().toIntOrNull()?.let { value -> playerBuilder.pointCost[value] ?: 0 } ?: 0
                    }

                    // Atualiza o TextView com os pontos restantes
                    val pointsRemaining = playerBuilder.pointBuyBalance - totalPointsSpent
                    remainingPointsTextView.text = "Pontos Restantes: $pointsRemaining"
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        builder.setView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            abilityInput.forEach { addView(it) }
            addView(remainingPointsTextView) // Adiciona o TextView de pontos restantes
        })

        builder.setPositiveButton("OK") { _, _ ->
            val finalValues = mutableMapOf<String, Int>()
            var allFifteen = true

            for (i in abilities.indices) {
                val abilityValue = abilityInput[i].text.toString().toIntOrNull()
                if (abilityValue in 8..15) {
                    finalValues[abilities[i]] = abilityValue!!
                    if (abilityValue != 15) {
                        allFifteen = false
                    }
                } else {
                    allFifteen = false
                }
            }

            if (allFifteen) {
                Toast.makeText(this, "Erro: Todos os valores não podem ser 15.", Toast.LENGTH_SHORT).show()
            } else if (totalPointsSpent > 27) {
                Toast.makeText(this, "Erro: O total de pontos não pode ultrapassar 27.", Toast.LENGTH_SHORT).show()
            } else {
                playerBuilder.assignAbilities(player, finalValues)
                Toast.makeText(this, "Habilidades definidas!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
