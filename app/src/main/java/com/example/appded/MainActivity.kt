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

        selectAbilitiesButton.setOnClickListener {
            showAbilitySelectionDialog()
        }

        createPlayerButton.setOnClickListener {
            val playerName = nameInput.text.toString()
            if (playerName.isEmpty() || selectedRace == null) {
                Toast.makeText(this, "Please enter a name and select a race.", Toast.LENGTH_SHORT).show()
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
            .setTitle("Select a Race")
            .setItems(raceNames) { _, which ->
                selectedRace = races[which]
                Toast.makeText(this, "Selected Race: ${selectedRace?.name}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAbilitySelectionDialog() {
        val abilities = playerBuilder.abilitiesSample
        val selectedValues = IntArray(abilities.size)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Abilities")

        builder.setMultiChoiceItems(abilities, selectedValues.map { it != 0 }.toBooleanArray()) { dialog, which, isChecked ->
            if (isChecked) {
                selectedValues[which] = 8 // valor padrÃ£o ao selecionar
            } else {
                selectedValues[which] = 0 // reiniciar valor ao desmarcar
            }
        }

        builder.setPositiveButton("Next") { _, _ ->
            showAbilityValueInputDialog(abilities, selectedValues)
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showAbilityValueInputDialog(abilities: Array<String>, selectedValues: IntArray) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Ability Values")

        val abilityInput = Array(abilities.size) { EditText(this) }

        abilities.forEachIndexed { index, ability ->
            abilityInput[index].hint = "$ability (8-15)"
            abilityInput[index].inputType = android.text.InputType.TYPE_CLASS_NUMBER
            if (selectedValues[index] != 0) {
                abilityInput[index].setText(selectedValues[index].toString())
            }
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
            Toast.makeText(this, "Abilities set!", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}