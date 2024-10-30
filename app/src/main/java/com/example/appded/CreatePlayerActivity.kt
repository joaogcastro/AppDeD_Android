package com.example.appded

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appded.database.AppDatabase
import com.example.appded.player.Player
import com.example.appded.player.PlayerBuilder
import com.example.appded.player.PlayerController
import kotlinx.coroutines.launch
import races.*

class CreatePlayerActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var createPlayerButton: Button
    private lateinit var selectRaceButton: Button
    private lateinit var selectAbilitiesButton: Button
    private lateinit var backButton: Button
    private lateinit var playerStatusTextView: TextView
    private lateinit var playersTextView: TextView
    private var selectedRace: Race? = null
    private val playerBuilder = PlayerBuilder()
    private val player = Player()
    private lateinit var database: AppDatabase
    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_player) // Use o layout correto

        // Inicializa o banco de dados
        database = AppDatabase.getDatabase(applicationContext)
        playerController = PlayerController(database.playerDao())

        // Referências às views
        nameInput = findViewById(R.id.nameInput)
        selectRaceButton = findViewById(R.id.selectRaceButton)
        selectAbilitiesButton = findViewById(R.id.selectAbilitiesButton)
        playerStatusTextView = findViewById(R.id.playerStatusTextView)
        backButton = findViewById(R.id.backButton)

        // Configura os listeners
        selectRaceButton.setOnClickListener { showRaceSelectionDialog() }
        selectAbilitiesButton.setOnClickListener { showAbilityValueInputDialog() }
        createPlayerButton = findViewById(R.id.createPlayerButton)
        createPlayerButton.setOnClickListener { createPlayer() }
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun createPlayer() {
        val playerName = nameInput.text.toString()
        if (playerName.isEmpty() || selectedRace == null) {
            Toast.makeText(this, "Por favor, insira um nome e selecione uma raça.", Toast.LENGTH_SHORT).show()
            return
        }
        player.name = playerName
        player.race = selectedRace
        playerBuilder.setRaceModifiers(player)
        playerBuilder.setHealthPoints(player)
        playerStatusTextView.text = playerBuilder.getStatus(player)

        lifecycleScope.launch {
            try {
                playerController.save(player)
                Toast.makeText(this@CreatePlayerActivity, "Player salvo com sucesso!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@CreatePlayerActivity, "Erro ao salvar o player: $e", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRaceSelectionDialog() {
        val races = playerBuilder.races
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
        var totalPointsSpent = 0

        abilities.forEachIndexed { index, ability ->
            abilityInput[index].hint = "$ability (8-15)"
            abilityInput[index].inputType = android.text.InputType.TYPE_CLASS_NUMBER
            abilityInput[index].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    totalPointsSpent = abilityInput.sumOf {
                        it.text.toString().toIntOrNull()?.let { value -> playerBuilder.pointCost[value] ?: 0 } ?: 0
                    }

                    val pointsRemaining = playerBuilder.pointBuyBalance - totalPointsSpent
                    remainingPointsTextView.text = "Pontos Restantes: $pointsRemaining"
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        builder.setView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            abilityInput.forEach { addView(it) }
            addView(remainingPointsTextView)
        })

        builder.setPositiveButton("OK") { _, _ ->
            val finalValues = mutableMapOf<String, Int>()

            for (i in abilities.indices) {
                val abilityValue = abilityInput[i].text.toString().toIntOrNull()
                if (abilityValue == null || abilityValue < 8 || abilityValue > 15) {
                    Toast.makeText(this, "Valor para ${abilities[i]} deve ser entre 8 e 15.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                finalValues[abilities[i]] = abilityValue
            }

            if (totalPointsSpent > 27) {
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
