package cz.fungisoft.coffeecompass2.activity.ui.notification;

public interface FragmentRemovableListener {
    /**
     * callback to be performed, when user click on delete button of the fragment
     * @param fragment
     */
    void  onFragmentClosed(SelectedTownFragment fragment);
}
