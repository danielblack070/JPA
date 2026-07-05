package com.ace.jp.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.app.data.Repository
import com.ace.jp.app.data.model.MasterRule
import com.ace.jp.app.data.model.SubRule
import com.ace.jp.app.data.model.Type
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConjugationViewModel(private val repository: Repository) : ViewModel() {
    private val _masterRules = MutableStateFlow<List<MasterRule>>(emptyList())
    val masterRules: StateFlow<List<MasterRule>> = _masterRules

    private val _subRules = MutableStateFlow<List<SubRule>>(emptyList())
    val subRules: StateFlow<List<SubRule>> = _subRules

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _masterRules.value = repository.getAllMasterRules()
            _subRules.value = repository.getAllSubRules()
        }
    }

    // --- MASTER RULE ACTIONS ---
    fun addMasterRule(name: String) {
        viewModelScope.launch {
            repository.insertMasterRule(MasterRule(name = name))
            loadData()
        }
    }

    fun updateMasterRule(masterRule: MasterRule) {
        viewModelScope.launch {
            repository.updateMasterRule(masterRule)
            loadData()
        }
    }

    fun deleteMasterRule(masterRule: MasterRule) {
        viewModelScope.launch {
            repository.deleteMasterRule(masterRule)
            loadData()
        }
    }

    fun deleteAllMasterRules() {
        viewModelScope.launch {
            repository.deleteAllMasterRules()
            loadData()
        }
    }

    // --- SUB-RULE ACTIONS ---
    fun addSubRule(masterRuleId: Int, type: Type, originalEnding: String?, newEnding: String, isUnique: Boolean) {
        viewModelScope.launch {
            repository.insertSubRule(
                SubRule(
                    masterRuleId = masterRuleId,
                    type = type,
                    originalEnding = originalEnding,
                    newEnding = newEnding,
                    isUnique = isUnique
                )
            )
            loadData()
        }
    }

    fun updateSubRule(subRule: SubRule) {
        viewModelScope.launch {
            repository.updateSubRule(subRule)
            loadData()
        }
    }

    fun deleteSubRule(subRule: SubRule) {
        viewModelScope.launch {
            repository.deleteSubRule(subRule)
            loadData()
        }
    }

    fun deleteSubRulesForMasterRule(masterRuleId: Int) {
        viewModelScope.launch {
            repository.deleteSubRulesForMasterRule(masterRuleId)
            loadData()
        }
    }
}