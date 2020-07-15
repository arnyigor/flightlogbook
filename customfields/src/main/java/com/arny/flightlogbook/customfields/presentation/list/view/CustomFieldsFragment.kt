package com.arny.flightlogbook.customfields.presentation.list.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.customfields.R
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import kotlinx.android.synthetic.main.fragment_custom_fields.*
import moxy.MvpAppCompatFragment

class CustomFieldsFragment : MvpAppCompatFragment() {

    private lateinit var customFieldsAdapter: CustomFieldsAdapter
    private var count = 100L

    companion object {
        @JvmStatic
        fun getInstance() = CustomFieldsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_fields, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.custom_fields)
        customFieldsAdapter = CustomFieldsAdapter()
        rvFieldsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = customFieldsAdapter
        }
        customFieldsAdapter.setViewHolderListener(object : SimpleAbstractAdapter.OnViewHolderListener<CustomField> {
            override fun onItemClick(position: Int, item: CustomField) {
                if (count > 0) {
                    val id = count
                    customFieldsAdapter.add(CustomField(id, "Custom field $id", CustomFieldType
                            .TYPE_BOOLEAN))
                    count--
                }
            }
        })
        val list = listOf(
                CustomField(1, "Количество посадок", CustomFieldType.TYPE_NUMBER_INT),
                CustomField(2, "Подвеска для вертолета", CustomFieldType.TYPE_BOOLEAN)
        )
        customFieldsAdapter.addAll(list)
    }
}
