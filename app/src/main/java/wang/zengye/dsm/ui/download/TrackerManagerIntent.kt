package wang.zengye.dsm.ui.download

import wang.zengye.dsm.ui.base.BaseIntent

sealed class TrackerManagerIntent : BaseIntent {
    data class LoadTrackers(val taskId: String) : TrackerManagerIntent()
    data object ShowAddDialog : TrackerManagerIntent()
    data object HideDialog : TrackerManagerIntent()
    data class UpdateNewTrackerUrl(val url: String) : TrackerManagerIntent()
    data class AddTracker(val taskId: String) : TrackerManagerIntent()
}
