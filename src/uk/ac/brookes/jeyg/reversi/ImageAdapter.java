package uk.ac.brookes.jeyg.reversi;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	
	private Context context;
	private int[] board;
	private int blackTile;
	private int whiteTile;
	
	public ImageAdapter(Context c, int[] board, int blackTile, int whiteTile) {
		context = c;
		this.board = board;
		this.blackTile = blackTile;
		this.whiteTile = whiteTile;
	}
	
	@Override
	public int getCount() {
		return board.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
	    if (convertView == null) {
	          imageView = new ImageView(context);
	          imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	          imageView.setAdjustViewBounds(true);
	          imageView.setPadding(1, 1, 1, 1);	          
	      } else {
	          imageView = (ImageView) convertView;
	     }
	     switch (board[position]) {
	          case 1: imageView.setImageResource(blackTile); break;
	          case 2: imageView.setImageResource(whiteTile); break;
	          default: imageView.setImageResource(R.drawable.empty); break;
	      }
	      return imageView;
	}

}
