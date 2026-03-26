
import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GameUI extends JFrame {
	
	JButton[][] cells = new JButton[8][8]; // tao ma tran nut bam
	Random rd = new Random();
	
	int selectedRow =-1;
	int selectedCol = -1;
	
	public void handleClick(int r, int c)
	{
		if(selectedRow == -1)
		{
			selectedRow = r;
			selectedCol = c;
	        cells[r][c].setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));

			
		}else {
	        cells[selectedRow][selectedCol].setBorder(UIManager.getBorder("Button.border"));

			if(checkKeNhau(selectedRow, selectedCol, r, c)){
	            swap(selectedRow, selectedCol, r, c);
	        }
			selectedRow = -1;
			selectedCol = -1;
		}
	}
	void swap(int r1,int c1,int r2,int c2){

	    Color color1 = cells[r1][c1].getBackground();
	    Color color2 = cells[r2][c2].getBackground();
	    
	    cells[r1][c1].setBackground(color2);
	    cells[r2][c2].setBackground(color1);
	    
	    if(checkMatch(r1,c1) || checkMatch(r2,c2))
	    {
//	    	while(true)
//	    	{
//	    		destroyMatch();
//	    		applyGravity();
//	    		spawnFromTop();
//	    		
//	    		
//	    		boolean found = false;
//	    		for(int i=0; i<8;i++)
//	    		{
//	    			for(int j=0; j<8;j++)
//	    			{
//	    				if(checkMatch(i,j))
//	    				{
//	    					found = true;
//	    				}
//	    			}
//	    		}
//	    		if(!found) break;
//	    	}
	    	
    	    animateDestroy1();
    	    
	    	
	    }else {
	    	
	    	cells[r1][c1].setBackground(color1);
	    	cells[r2][c2].setBackground(color2);
	    }
	}
	boolean checkKeNhau(int r1,int c1,int r2,int c2){

	    int diff = Math.abs(r1 - r2) + Math.abs(c1 - c2);

	    return diff == 1;

	}
	
	boolean checkMatch(int r,int c)
	{
		Color color = cells[r][c].getBackground();
		int cnt = 1;
		
	    if(color.equals(Color.WHITE)) return false;

		
		// ngang trai
		if(c-1>=0)
		{
			int j=c-1;
			while(j>=0 && cells[r][j].getBackground().equals(color))
			{
				cnt++;
				j--;
			}
		}
		if(c+1>=0)
		{
			int j=c+1;
			while(j<8 && cells[r][j].getBackground().equals(color))
			{
				cnt++;
				j++;
			}
		}
		if (cnt>=3) return true;
		cnt=1;
		
		//doc tren
		if(r-1>=0)
		{
			int i=r-1;
			while(i >= 0 && cells[i][c].getBackground().equals(color)){
		        cnt++;
		        i--;
			}
		}
		
		// doc xuong
		
		if(r+1<8)
		{
			 int i = r + 1;
			    while(i < 8 && cells[i][c].getBackground().equals(color)){
			        cnt++;
			        i++;
			    }
		}
		return cnt>=3;
	}
	
	
	void applyGravity()
	{
		for(int j=0; j<8;j++)
		{
			for(int i = 7; i>=0; i-- )
			{
				if(cells[i][j].getBackground().equals(Color.WHITE))
				{
					int k = i-1;
					while(k>=0 && cells[k][j].getBackground().equals(Color.WHITE))
					{
						k--;
					}
					if(k>=0)
					{
						cells[i][j].setBackground(cells[k][j].getBackground());
						cells[k][j].setBackground(Color.WHITE);
					}
				}
			}
		}
		
	}
	
	
	void spawnFromTop()
	{
		for(int j=0; j<8;j++)
		{
			for(int i=0; i<8;i++)
			{
				if(cells[i][j].getBackground().equals(Color.WHITE))
				{
					int candy = rd.nextInt(5);
					
					if(candy == 0) cells[i][j].setBackground(Color.RED);
					if(candy == 1) cells[i][j].setBackground(Color.BLUE);
					if(candy == 2) cells[i][j].setBackground(Color.YELLOW);
					if(candy == 3) cells[i][j].setBackground(Color.GREEN);
					if(candy == 4) cells[i][j].setBackground(Color.ORANGE);

					
				}
			}
		}
	}
	
	
	void destroyMatch()
	{
		boolean[][] mark  = new boolean[8][8];
		
		//check ngang
		for(int i=0;i<8;i++)
		{
			for(int j=0; j<6;j++)
			{
				Color c = cells[i][j].getBackground();
				if(c.equals(cells[i][j+1].getBackground()) && c.equals(cells[i][j+2].getBackground())) {
					mark[i][j] = true;
					mark[i][j+1]=true;
					mark[i][j+2]= true;
				}
			}
		}
		//check doc
		for(int i=0;i<6;i++)
		{
			for(int j=0; j<8;j++)
			{
				Color c = cells[i][j].getBackground();
				if(c.equals(cells[i+1][j].getBackground()) && c.equals(cells[i+2][j].getBackground())) {
					mark[i][j] = true;
					mark[i+1][j]=true;
					mark[i+2][j]= true;
				}
			}
		}
		
		for(int i=0; i<8;i++)
		{
			for(int j=0; j<8;j++)
			{
				if(mark[i][j])  cells[i][j].setBackground(Color.white);
			}
		}
		
	}
	
	void animateDestroy1(){

	    boolean[][] mark = new boolean[8][8];

	    // check ngang
	    for(int i=0;i<8;i++){
	        for(int j=0;j<6;j++){
	            Color c = cells[i][j].getBackground();

	            if(!c.equals(Color.WHITE) &&
	               c.equals(cells[i][j+1].getBackground()) &&
	               c.equals(cells[i][j+2].getBackground())){

	                mark[i][j] = mark[i][j+1] = mark[i][j+2] = true;
	            }
	        }
	    }

	    // check dọc
	    for(int i=0;i<6;i++){
	        for(int j=0;j<8;j++){
	            Color c = cells[i][j].getBackground();

	            if(!c.equals(Color.WHITE) &&
	               c.equals(cells[i+1][j].getBackground()) &&
	               c.equals(cells[i+2][j].getBackground())){

	                mark[i][j] = mark[i+1][j] = mark[i+2][j] = true;
	            }
	        }
	    }

	    // 💥 hiệu ứng flash
	    Timer flash = new Timer(100, null);

	    flash.addActionListener(e -> {

	        for(int i=0;i<8;i++){
	            for(int j=0;j<8;j++){
	                if(mark[i][j]){
	                    cells[i][j].setBackground(Color.WHITE);
	                }
	            }
	        }

	        ((Timer)e.getSource()).stop();

	        // sau khi phá → rơi
	        animateGravity();
	    });

	    flash.setRepeats(false);
	    flash.start();
	}
	
	
	
	boolean fallStep(){

	    boolean moved = false;

	    for(int j=0;j<8;j++){
	        for(int i=7;i>0;i--){

	            if(cells[i][j].getBackground().equals(Color.WHITE) &&
	               !cells[i-1][j].getBackground().equals(Color.WHITE)){

	                // kéo xuống 1 ô
	                cells[i][j].setBackground(cells[i-1][j].getBackground());
	                cells[i-1][j].setBackground(Color.WHITE);

	                moved = true;
	            }
	        }
	    }

	    return moved;
	}
	
	boolean hasAnyMatch(){

	    for(int i=0;i<8;i++){
	        for(int j=0;j<8;j++){
	            if(checkMatch(i,j)) return true;
	        }
	    }

	    return false;
	}
	
	void animateDestroy(){

	    destroyMatch();

	    Timer timer = new Timer(150, null);

	    timer.addActionListener(e -> {
	        ((Timer)e.getSource()).stop();
	        animateGravity();
	    });

	    timer.setRepeats(false);
	    timer.start();
	}
	
	void animateGravity(){

	    Timer timer = new Timer(80, null); // 80ms mỗi frame

	    timer.addActionListener(e -> {

	        boolean moved = fallStep();

	        if(!moved){
	            timer.stop();

	            spawnFromTop();

	            // sau khi spawn, check combo tiếp
	            if(hasAnyMatch()){
	                animateDestroy1(); // gọi lại vòng lặp
	            }
	        }
	    });

	    timer.start();
	}
	
	
	
	
	
	
	
	
	
	
	public GameUI() {
		setTitle("Puzzle Insight");
		setSize(500,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		JPanel broadPanel = new JPanel();
		broadPanel.setLayout(new GridLayout(8,8));
		
		for(int i=0; i<8;i++)
		{
			for(int j=0;j<8;j++)
			{
				cells[i][j] = new JButton();
				int row = i,col=j;
				cells[i][j].addActionListener(e->handleClick(row,col));
				
				int candy = rd.nextInt(5);
				if(candy==0) cells[i][j].setBackground(Color.red);
				if(candy==1) cells[i][j].setBackground(Color.blue);
				if(candy==2) cells[i][j].setBackground(Color.yellow);
				if(candy==3) cells[i][j].setBackground(Color.green);
				if(candy==4) cells[i][j].setBackground(Color.orange);

				broadPanel.add(cells[i][j]);
			}
		}
		
		add(broadPanel,BorderLayout.CENTER);
		setVisible(true);
		
	}
}
