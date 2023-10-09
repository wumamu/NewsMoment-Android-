package com.recoveryrecord.surveyandroid.example.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.recoveryrecord.surveyandroid.example.R
import com.recoveryrecord.surveyandroid.example.adapter.NewsCategoryPagerAdapter
import com.recoveryrecord.surveyandroid.example.ui.Util.mediaMap

const val MEDIA_SOURCE = "media_source"

class MainFragment : Fragment() {
    private var source: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            source = it.getString(MEDIA_SOURCE, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.nested_layer1_media, container, false)
        val mViewPager = view.findViewById<View>(R.id.container_main) as ViewPager
        val mNewsCategoryPagerAdapter =
            NewsCategoryPagerAdapter(childFragmentManager, source, mediaMap[source] ?: emptyMap())
        mViewPager.adapter = mNewsCategoryPagerAdapter
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(source: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(MEDIA_SOURCE, source)
                }
            }

    }
}