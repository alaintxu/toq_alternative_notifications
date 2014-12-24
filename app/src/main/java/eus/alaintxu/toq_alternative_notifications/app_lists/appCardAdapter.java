package eus.alaintxu.toq_alternative_notifications.app_lists;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eus.alaintxu.toq_alternative_notifications.R;

/**
 * Created by aperez on 22/12/14.
 */
public class appCardAdapter  extends RecyclerView.Adapter<appCardAdapter.ViewHolder> {
    private ArrayList<MyApplicationInfo> mDataset;
    //private MyApplicationInfo[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public appCardAdapter(ArrayList<MyApplicationInfo> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public appCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_card, parent, false);
        ViewHolder vh = new ViewHolder((CardView)v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mCardView.setText(mDataset[position].title);
        //CardView v = holder.mCardView;
        MyApplicationInfo app = mDataset.get(position);
        if(app != null){
            CheckBox appCB = (CheckBox) holder.mCardView.findViewById(R.id.appCB);
            LinearLayout cardLL = (LinearLayout) holder.mCardView.findViewById(R.id.card_ll);

            TextView appTitle = (TextView) holder.mCardView.findViewById(R.id.appTitleTV);
            TextView appPkg = (TextView) holder.mCardView.findViewById(R.id.appPackageTv);
            ImageView appIcon = (ImageView) holder.mCardView.findViewById(R.id.appIconIV);

            if (appCB != null){
                appCB.setTag(app.getPkg());
                appCB.setChecked(app.getNotify());
                appCB.setEnabled(app.getEnabled());
            }
            if (appTitle != null){
                appTitle.setText(app.getTitle());
            }

            if (appPkg != null){
                appPkg.setText(app.getPkg());
            }

            if (appIcon != null){
                appIcon.setImageDrawable(app.getIcon());
            }

            int resourceTo;
            int resourceFrom;
            float alpha = (float) 1;
            if (!app.getEnabled()){
                alpha = (float) 0.25;
            }

            if (app.getNotify()){
                resourceTo = R.color.cardSelected;
                resourceFrom = R.color.cardNotSelected;
            }else{
                resourceTo = R.color.cardNotSelected;
                resourceFrom = R.color.cardSelected;
            }

            cardLL.setBackgroundColor(holder.mCardView.getResources().getColor(resourceTo));
            cardLL.setAlpha(alpha);

            int myAnimation = app.getAnimation();
            if (myAnimation != MyApplicationInfo.NO_ANIMATION){

                Animation animation = AnimationUtils.loadAnimation(holder.mCardView.getContext(), myAnimation);
                holder.itemView.startAnimation(animation);
            }
        }

        //return v;

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
