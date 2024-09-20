package com.example.roworktracker.fragmentsMain

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.roworktracker.MainActivity
import com.example.roworktracker.R
import com.example.roworktracker.ToDosManageActivity


class FragmentMenu : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout only once
        val view = inflater.inflate(R.layout.fragment_menu, container, false)



        return view // Return the inflated view
    }


}
