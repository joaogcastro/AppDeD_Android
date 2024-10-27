// MainActivity.kt
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
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import races.*

class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var createPlayerButton: Button
    private lateinit var selectRaceButton: Button
    private lateinit var selectAbilitiesButton: Button
    private lateinit var savePlayerButton: Button
    private lateinit var editPlayerButton: Button
    private lateinit var deletePlayerButton: Button
    private lateinit var displayPlayersButton: Button
    private lateinit var playerStatusTextView: TextView
    private var selectedRace: Race? = null
    private val playerBuilder = PlayerBuilder()
    private val player = Player()
    private lateinit var database: AppDatabase
    private var currentPlayerId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)

        nameInput = findViewById(R.id.nameInput)
        createPlayerButton = findViewById(R.id.createPlayerButton)
        selectRaceButton = findViewById(R.id.selectRaceButton)
        selectAbilitiesButton = findViewById(R.id.selectAbilitiesButton)
        savePlayerButton = findViewById(R.id.savePlayerButton)
        editPlayerButton = findViewById(R.id.editPlayerButton)
        deletePlayerButton = findViewById(R.id.deletePlayerButton)
        displayPlayersButton = findViewById(R.id.displayPlayersButton)
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

        savePlayerButton.setOnClickListener {
            savePlayer()
        }

        editPlayerButton.setOnClickListener {
            editPlayer()
        }

        deletePlayerButton.setOnClickListener {
            deletePlayer()
        }

        displayPlayersButton.setOnClickListener {
            displayPlayers()
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

    private fun savePlayer() {
        lifecycleScope.launch {
            val playerEntity = PlayerEntity(
                id = currentPlayerId ?: 0, // Se for um novo jogador, o ID será 0
                name = player.name,
                race = selectedRace?.name ?: "",
                abilities = Gson().toJson(player.abilities),
                healthPoints = player.healthPoints
            )
            if (currentPlayerId == null) {
                database.playerDao().insert(playerEntity)
                Toast.makeText(this@MainActivity, "Personagem salvo!", Toast.LENGTH_SHORT).show()
            } else {
                database.playerDao().update(playerEntity)
                Toast.makeText(this@MainActivity, "Personagem atualizado!", Toast.LENGTH_SHORT).show()
            }
            clearInputFields()
        }
    }

    private fun editPlayer() {
        lifecycleScope.launch {
            val players = database.playerDao().getAllPlayers()
            val playerNames = players.map { it.name }.toTypedArray()

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Escolha um personagem para editar")
                .setItems(playerNames) { _, which ->
                    val selectedPlayer = players[which]
                    currentPlayerId = selectedPlayer.id
                    player.name = selectedPlayer.name
                    selectedRace = playerBuilder.races.firstOrNull { it.name == selectedPlayer.race } // Corrigido
                    player.abilities = Gson().fromJson(selectedPlayer.abilities, object : TypeToken<Map<String, Int>>() {}.type)
                    player.healthPoints = selectedPlayer.healthPoints

                    nameInput.setText(player.name)
                    playerStatusTextView.text = playerBuilder.getStatus(player)
                    Toast.makeText(this@MainActivity, "Personagem carregado para edição!", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    private fun deletePlayer() {
        lifecycleScope.launch {
            val players = database.playerDao().getAllPlayers()
            val playerNames = players.map { it.name }.toTypedArray()

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Escolha um personagem para excluir")
                .setItems(playerNames) { _, which ->
                    val selectedPlayer = players[which]

                    // Confirmação antes da exclusão
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Confirmação")
                        .setMessage("Você tem certeza que deseja excluir ${selectedPlayer.name}?")
                        .setPositiveButton("Sim") { _, _ ->
                            lifecycleScope.launch {
                                database.playerDao().delete(selectedPlayer)
                                Toast.makeText(this@MainActivity, "Personagem excluído!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                .show()
        }
    }


    private fun displayPlayers() {
        lifecycleScope.launch {
            val players = database.playerDao().getAllPlayers()
            val playerNames = players.joinToString("\n") { it.name }
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Personagens Salvos")
                .setMessage(playerNames.ifEmpty { "Nenhum personagem salvo." })
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun clearInputFields() {
        nameInput.text.clear()
        selectedRace = null
        player.abilities.clear()
        player.healthPoints = 10
        playerStatusTextView.text = "O status do personagem aparecerá aqui"
        currentPlayerId = null
    }
}
