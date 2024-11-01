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
import com.example.appded.player.PlayerController
import com.example.appded.utils.abilitiesSample
import com.example.appded.utils.assignAbilities
import com.example.appded.utils.getStatus
import com.example.appded.utils.pointCost
import com.example.appded.utils.races
import com.example.appded.utils.setHealthPoints
import com.example.appded.utils.setRaceModifiers
import com.example.appded.utils.unSetRaceModifiers
import kotlinx.coroutines.launch
import races.*

class EditPlayerActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var selectRaceButton: Button
    private lateinit var selectAbilitiesButton: Button
    private lateinit var saveChangesButton: Button
    private lateinit var backButton: Button
    private lateinit var playerStatusTextView: TextView
    private lateinit var database: AppDatabase
    private lateinit var playerController: PlayerController
    private var currentPlayer: Player? = null
    private var selectedRace: Race? = null
    private var isAbilitiesChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_player)

        // Instancia do db
        database = AppDatabase.getDatabase(applicationContext)
        playerController = PlayerController(database.playerDao())

        // Referencia as views
        nameInput = findViewById(R.id.nameInput)
        selectRaceButton = findViewById(R.id.selectRaceButton)
        selectAbilitiesButton = findViewById(R.id.selectAbilitiesButton)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        backButton = findViewById(R.id.backButton)
        playerStatusTextView = findViewById(R.id.playerStatusTextView)

        // Configura os listeners
        selectRaceButton.setOnClickListener { showRaceSelectionDialog() }
        selectAbilitiesButton.setOnClickListener { showAbilityValueInputDialog() }
        saveChangesButton.setOnClickListener { savePlayerChanges() }
        backButton.setOnClickListener { finish() }

        // Carrega o jogador selecionado a partir do Intent
        val playerId = intent.getIntExtra("PLAYER_ID", -1)
        if (playerId == -1) {
            Toast.makeText(this, "ID de jogador invalido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPlayer(playerId)
    }

    private fun loadPlayer(playerId: Int) {
        lifecycleScope.launch {
            currentPlayer = playerController.getPlayerById(playerId)
            if(currentPlayer != null) {
                nameInput.setText(currentPlayer!!.name)
                selectedRace = currentPlayer!!.race
                playerStatusTextView.text = getStatus(currentPlayer!!)
            } else {
                Toast.makeText(this@EditPlayerActivity, "Personagem não encontrado no banco de dados", Toast.LENGTH_SHORT).show()
                this@EditPlayerActivity.finish()
            }
        }
    }

    private fun savePlayerChanges() {
        val playerName = nameInput.text.toString()
        if (playerName.isEmpty() || selectedRace == null) {
            Toast.makeText(this, "Por favor, insira um nome e selecione uma raca.", Toast.LENGTH_SHORT).show()
            return
        }

        if(!isAbilitiesChanged) { unSetRaceModifiers(currentPlayer!!) }

        currentPlayer!!.name = playerName
        currentPlayer!!.race = selectedRace
        setRaceModifiers(currentPlayer!!)
        setHealthPoints(currentPlayer!!)
        playerStatusTextView.text = getStatus(currentPlayer!!)
        isAbilitiesChanged = false

        lifecycleScope.launch {
            try {
                playerController.update(currentPlayer!!)
                Toast.makeText(this@EditPlayerActivity, "Player atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@EditPlayerActivity, "Erro ao atualizar o player: $e", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRaceSelectionDialog() {
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

        val abilities = abilitiesSample
        val abilityInput = Array(abilities.size) { EditText(this) }
        val remainingPointsTextView = TextView(this)
        var pointsRemaining = 27
        var totalPointsSpent = 0

        abilities.forEachIndexed { index, ability ->
            abilityInput[index].hint = "$ability (8-15)"
            abilityInput[index].inputType = android.text.InputType.TYPE_CLASS_NUMBER
            abilityInput[index].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    totalPointsSpent = abilityInput.sumOf {
                        it.text.toString().toIntOrNull()?.let { value -> pointCost[value] ?: 0 } ?: 0
                    }
                    pointsRemaining -= totalPointsSpent
                    remainingPointsTextView.text = "Pontos Restantes: $pointsRemaining"
                }
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
                Toast.makeText(this, "Erro: O total de pontos nao pode ultrapassar 27.", Toast.LENGTH_SHORT).show()
            } else {
                assignAbilities(currentPlayer!!, finalValues)
                isAbilitiesChanged = true
                Toast.makeText(this, "Habilidades definidas!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
