package com.sum.main.ui.home

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.sum.common.constant.KEY_VIDEO_PLAY_LIST
import com.sum.common.constant.VIDEO_ACTIVITY_PLAYER
import com.sum.framework.decoration.StaggeredItemDecoration
import com.sum.framework.base.BaseMvvmFragment
import com.sum.framework.ext.gone
import com.sum.framework.ext.visible
import com.sum.framework.log.LogUtil
import com.sum.framework.toast.TipsToast
import com.sum.framework.utils.dpToPx
import com.sum.main.databinding.FragmentHomeVideoBinding
import com.sum.main.ui.home.adapter.HomeVideoItemAdapter
import com.sum.main.ui.home.viewmodel.HomeViewModel
import com.sum.room.entity.VideoInfo
import java.util.ArrayList

/**
 * @author mingyan.su
 * @date   2023/3/5 20:11
 * @desc   首页视频列表
 */
class HomeVideoFragment : BaseMvvmFragment<FragmentHomeVideoBinding, HomeViewModel>() {
    lateinit var videoAdapter: HomeVideoItemAdapter
    override fun initView(view: View, savedInstanceState: Bundle?) {

        val spanCount = 2
        val manager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        videoAdapter = HomeVideoItemAdapter(requireContext())
        mBinding?.recyclerView?.apply {
            layoutManager = manager
            addItemDecoration(StaggeredItemDecoration(dpToPx(10)))
            adapter = videoAdapter
        }

        videoAdapter.onItemClickListener = { view: View, position: Int ->
            val multiplePermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions.all { it.value }) {
                    // 所有权限都被授予
                    ARouter.getInstance().build(VIDEO_ACTIVITY_PLAYER)
                        .withParcelableArrayList(KEY_VIDEO_PLAY_LIST, videoAdapter.getData() as ArrayList<VideoInfo>)
                        .navigation()
                } else {
                    // 至少有一个权限被拒绝
                    TipsToast.showTips(com.sum.common.R.string.default_agree_permission)
                }
            }

            // 请求多个权限
            multiplePermissionLauncher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    override fun initData() {
        showLoading()
        mViewModel.getVideoList(requireContext().assets).observe(this) {
            dismissLoading()
            if (it.isNullOrEmpty()) {
                mBinding?.viewEmptyData?.visible()
            } else {
                mBinding?.viewEmptyData?.gone()
                videoAdapter.setData(it)
            }
        }
    }
}