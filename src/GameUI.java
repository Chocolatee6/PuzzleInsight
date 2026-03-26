
import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class GameUI extends JFrame {
	
	JButton[][] cells = new JButton[8][8]; // tao ma tran nut bam
	Random rd = new Random();

	ImageIcon[] characterIcons = new ImageIcon[5];
	
	int selectedRow =-1;
	int selectedCol = -1;
	
	ImageIcon loadIcon(String path)
	{
		ImageIcon icon = new ImageIcon(path);
		Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
		return new ImageIcon(img);
	}


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

    int type1 = (int) cells[r1][c1].getClientProperty("type");
    int type2 = (int) cells[r2][c2].getClientProperty("type");

    // swap dữ liệu
    cells[r1][c1].putClientProperty("type", type2);
    cells[r2][c2].putClientProperty("type", type1);

    // cập nhật hình
    cells[r1][c1].setIcon(characterIcons[type2]);
    cells[r2][c2].setIcon(characterIcons[type1]);

    // check match
    if(checkMatch(r1,c1) || checkMatch(r2,c2)){
        animateDestroy();
    } else {
        // đổi lại nếu không hợp lệ
        cells[r1][c1].putClientProperty("type", type1);
        cells[r2][c2].putClientProperty("type", type2);

        cells[r1][c1].setIcon(characterIcons[type1]);
        cells[r2][c2].setIcon(characterIcons[type2]);
    }
}
	boolean checkKeNhau(int r1,int c1,int r2,int c2){

	    int diff = Math.abs(r1 - r2) + Math.abs(c1 - c2);

	    return diff == 1;

	}
	
	boolean checkMatch(int r,int c)
	{
		
		int cnt = 1;
		int type = (int) cells[r][c].getClientProperty("type");
		
	    if(type == -1) return false;

		
		// ngang trai
		if(c-1>=0)
		{
			int j=c-1;
			while(j>=0 && (int)cells[r][j].getClientProperty("type")==type)
			{
				cnt++;
				j--;
			}
		}
		if(c+1>=0)
		{
			int j=c+1;
			while(j<8 && (int)cells[r][j].getClientProperty("type")==type)
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
			while(i >= 0 && (int)cells[i][c].getClientProperty("type")==type){
		        cnt++;
		        i--;
			}
		}
		
		// doc xuong
		
		if(r+1<8)
		{
			 int i = r + 1;
			    while(i < 8 && (int)cells[i][c].getClientProperty("type")==type){
			        cnt++;
			        i++;
			    }
		}
		return cnt>=3;
	}
	
	
	void applyGravity(){

    for(int j=0;j<8;j++){
        for(int i=7;i>=0;i--){

            int type = (int) cells[i][j].getClientProperty("type");

            if(type == -1){

                int k = i - 1;

                while(k >= 0 &&
                    (int)cells[k][j].getClientProperty("type") == -1){
                    k--;
                }

                if(k >= 0){

                    int aboveType = (int) cells[k][j].getClientProperty("type");

                    cells[i][j].putClientProperty("type", aboveType);
                    cells[i][j].setIcon(characterIcons[aboveType]);

                    cells[k][j].putClientProperty("type", -1);
                    cells[k][j].setIcon(null);
                }
            }
        }
    }
}
	
	
	void spawnFromTop(){

    for(int j=0;j<8;j++){
        for(int i=0;i<8;i++){

            int type = (int) cells[i][j].getClientProperty("type");

            if(type == -1){

                int newCandy = rd.nextInt(5);

                cells[i][j].putClientProperty("type", newCandy);
                cells[i][j].setIcon(characterIcons[newCandy]);
            }
        }
    }
}
	
	
	void destroyMatch() {

    boolean[][] mark = new boolean[8][8];

    // check ngang
    for(int i=0;i<8;i++){
        for(int j=0;j<6;j++){

            int type = (int) cells[i][j].getClientProperty("type");

            if(type != -1 &&
               type == (int)cells[i][j+1].getClientProperty("type") &&
               type == (int)cells[i][j+2].getClientProperty("type")){

                mark[i][j] = mark[i][j+1] = mark[i][j+2] = true;
            }
        }
    }

    // check dọc
    for(int i=0;i<6;i++){
        for(int j=0;j<8;j++){

            int type = (int) cells[i][j].getClientProperty("type");

            if(type != -1 &&
               type == (int)cells[i+1][j].getClientProperty("type") &&
               type == (int)cells[i+2][j].getClientProperty("type")){

                mark[i][j] = mark[i+1][j] = mark[i+2][j] = true;
            }
        }
    }

    // phá
    for(int i=0;i<8;i++){
        for(int j=0;j<8;j++){
            if(mark[i][j]){
                cells[i][j].putClientProperty("type", -1);
                cells[i][j].setIcon(null);
            }
        }
    }
}
	
	
	
	
	
	boolean fallStep(){

    boolean moved = false;

    for(int j=0;j<8;j++){
        for(int i=7;i>0;i--){

            int cur = (int) cells[i][j].getClientProperty("type");
            int above = (int) cells[i-1][j].getClientProperty("type");

            if(cur == -1 && above != -1){

                // kéo xuống 1 ô
                cells[i][j].putClientProperty("type", above);
                cells[i][j].setIcon(characterIcons[above]);

                cells[i-1][j].putClientProperty("type", -1);
                cells[i-1][j].setIcon(null);

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
	                animateDestroy(); // gọi lại vòng lặp
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
		
		JPanel broadPanel = new GradientPanel();
		broadPanel.setLayout(new GridLayout(8,8));
		
		for (int i = 0; i <5; i++) {
			characterIcons[i] = loadIcon("images/characters_000"+(i+1)+".png");
		}


		for(int i=0; i<8;i++)
		{
			for(int j=0;j<8;j++)
			{
				cells[i][j] = new JButton();
				cells[i][j].setPreferredSize(new Dimension(60,60));
				cells[i][j].setBorderPainted(false);
				cells[i][j].setFocusPainted(false);
				cells[i][j].setContentAreaFilled(false);
				int row = i,col=j;
				cells[i][j].addActionListener(e->handleClick(row,col));
				
				int candy = rd.nextInt(5);

				cells[i][j].putClientProperty("type", candy);
				cells[i][j].setIcon(characterIcons[candy]);

				broadPanel.add(cells[i][j]);
			}
		}
		
		add(broadPanel,BorderLayout.CENTER);
		setVisible(true);
		
	}
}
