package com.example.appded

import android.app.AlertDialog
import android.os.Bundle
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

        // Removido o diálogo de seleção de habilidades
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

        abilities.forEachIndexed { index, ability ->
            abilityInput[index].hint = "$ability (8-15)"
            abilityInput[index].inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        builder.setView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            abilityInput.forEach { addView(it) }
        })

        builder.setPositiveButton("OK") { _, _ ->
            val finalValues = mutableMapOf<String, Int>()
            for (i in abilities.indices) {
                val abilityValue = abilityInput[i].text.toString().toIntOrNull()
                if (abilityValue in 8..15) {
                    finalValues[abilities[i]] = abilityValue!!
                }
            }
            playerBuilder.assignAbilities(player, finalValues)
            Toast.makeText(this, "Habilidades definidas!", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}