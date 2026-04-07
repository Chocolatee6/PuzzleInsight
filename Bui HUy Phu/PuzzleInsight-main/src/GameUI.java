
import java.awt.*;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
      
public class GameUI extends JPanel {
	Main main;
    JPanel gameOverPanel;
    boolean isLeveling = false;
	JButton[][] cells = new JButton[8][8]; // tao ma tran nut bam
	Random rd = new Random();
	boolean isProcessing = false;
	int score = 0;
	int moves = 20;
	int level = 1;
	int targetScore = 200;
	int scoreToReach = 200; // mốc cần đạt để lên level
    int combo = 0;
	Timer blinkTimer;
	boolean isBlinkOn = false;
	ImageIcon[] characterIcons = new ImageIcon[15];
	
	int selectedRow =-1;
	int selectedCol = -1;
	JLabel lblScore, lblMoves, lblLevel, lblTarget;
	void playSound(String path){

    // 🔇 nếu tắt sound thì không phát
    if(!main.soundOn) return;

    try{
        AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
        Clip clip = AudioSystem.getClip();
        clip.open(audio);
        clip.start();
    }catch(Exception e){
        e.printStackTrace();
    }
}

void createGameOverPanel(){

    gameOverPanel = new JPanel(){
        protected void paintComponent(Graphics g){
            super.paintComponent(g);

            // nền mờ
            g.setColor(new Color(0,0,0,150));
            g.fillRect(0,0,getWidth(),getHeight());
        }
    };

    gameOverPanel.setLayout(new GridBagLayout());

    JPanel box = new JPanel();
    box.setPreferredSize(new Dimension(250,200));
    box.setBackground(new Color(40,40,80));
    box.setLayout(new GridLayout(4,1,10,10));

    JLabel title = new JLabel("GAME OVER", SwingConstants.CENTER);
    title.setForeground(Color.WHITE);
    title.setFont(new Font("Arial", Font.BOLD, 20));

    JLabel scoreLbl = new JLabel("Score: " + score, SwingConstants.CENTER);
    scoreLbl.setForeground(Color.YELLOW);

    JButton btnReplay = new JButton("🔄 Chơi lại");
    JButton btnMenu = new JButton("🏠 Menu");

    // style nút
    btnReplay.setBackground(new Color(0,180,0));
    btnReplay.setForeground(Color.WHITE);

    btnMenu.setBackground(new Color(200,80,0));
    btnMenu.setForeground(Color.WHITE);

    // sự kiện
    btnReplay.addActionListener(e -> {
        gameOverPanel.setVisible(false);
        restartGame();
    });

    btnMenu.addActionListener(e -> {
        gameOverPanel.setVisible(false); 
        main.showScreen("MENU");
    });

    box.add(title);
    box.add(scoreLbl);
    box.add(btnReplay);
    box.add(btnMenu);

    gameOverPanel.add(box);

    gameOverPanel.setPreferredSize(new Dimension(500,600));
    gameOverPanel.setVisible(false);

    main.setGlassPane(gameOverPanel);
}
void restartGame(){
    score = 0;
    moves = 20;
    level = 1;
    scoreToReach = 200;

    selectedRow = -1;
    selectedCol = -1;

    if(blinkTimer != null) blinkTimer.stop();

    // tạo lại board
    do{
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                spawnOne(i,j);
            }
        }
    }while(hasAnyMatch()); // tránh auto match

    isProcessing = false;
    for(int i=0;i<8;i++){
        for(int j=0;j<8;j++){
            cells[i][j].setEnabled(true);
        }
    }
    updateUIInfo();
}

	void updateUIInfo(){
        lblScore.setText("Score: " + score + " (x" + combo + ")");
        lblMoves.setText("Moves: " + moves);
        lblLevel.setText("Level: " + level);
        lblTarget.setText("Target: " + scoreToReach);

        if(score >= scoreToReach && !isLeveling){
            isLeveling = true;
            nextLevel();
        }

        if(moves <= 0){
            disableBoard();
            showGameOverPanel();
        }
	}
    void showGameOverPanel(){

    // cập nhật score mới
    Component[] comps = ((JPanel)gameOverPanel.getComponent(0)).getComponents();
    JLabel scoreLbl = (JLabel) comps[1];
    scoreLbl.setText("Score: " + score);

    gameOverPanel.setVisible(true);
    gameOverPanel.setOpaque(false); // 👈 QUAN TRỌNG
    gameOverPanel.repaint();
}
	void disableBoard(){
    for(int i=0;i<8;i++){
        for(int j=0;j<8;j++){
            cells[i][j].setEnabled(false);
        }
    }
}
	ImageIcon loadIcon(String path)
	{
		ImageIcon icon = new ImageIcon(path);
		Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
		return new ImageIcon(img);
	}


	public void handleClick(int r, int c)
	{
	int type = (int) cells[r][c].getClientProperty("type");

	// 💣 nếu click bomb → nổ luôn
	if(type >= 5 && type < 10){
		explodeCross(r, c);
		cells[r][c].putClientProperty("type", -1);
		cells[r][c].setIcon(null);

		animateGravity();
		return;
	}
	if(isProcessing) return; // 👈 CHẶN
    if(selectedRow == -1)
    {
        selectedRow = r;
        selectedCol = c;

        startBlink(r, c); // 👈 chỉ cần dòng này

    }else {

        // 👇 dừng blink
        if(blinkTimer != null) blinkTimer.stop();
        cells[selectedRow][selectedCol].setBorder(null);

        if(checkKeNhau(selectedRow, selectedCol, r, c)){
            swap(selectedRow, selectedCol, r, c);
        }

        selectedRow = -1;
        selectedCol = -1;
    }
	}
	void startBlink(int r, int c){
    if(blinkTimer != null) blinkTimer.stop();

    isBlinkOn = false;

    blinkTimer = new Timer(250, e -> {
        if(isBlinkOn){
            cells[r][c].setBorder(null);
        } else {
            cells[r][c].setBorder(BorderFactory.createLineBorder(Color.RED, 4));
        }
        isBlinkOn = !isBlinkOn;
    });

    blinkTimer.start();
	}

	void swap(int r1,int c1,int r2,int c2){

    int type1 = (int) cells[r1][c1].getClientProperty("type");
    int type2 = (int) cells[r2][c2].getClientProperty("type");

    // đổi icon trước (giả animation nhẹ)
    cells[r1][c1].setIcon(characterIcons[type2]);
    cells[r2][c2].setIcon(characterIcons[type1]);

    Timer t = new Timer(150, null);

    t.addActionListener(e -> {
        ((Timer)e.getSource()).stop();

        // cập nhật type thật
        cells[r1][c1].putClientProperty("type", type2);
        cells[r2][c2].putClientProperty("type", type1);

        if(checkMatch(r1,c1) || checkMatch(r2,c2)){
            combo = 0; // 👈 reset trước khi bắt đầu chain mới
            animateDestroy();
        } else {
            // ❌ không match → trả về
            swapBack(r1,c1,r2,c2);
        }

        moves--;
        updateUIInfo();
    });

    t.setRepeats(false);
    t.start();
	}
	void swapBack(int r1,int c1,int r2,int c2){

    int type1 = (int) cells[r1][c1].getClientProperty("type");
    int type2 = (int) cells[r2][c2].getClientProperty("type");

    Timer t = new Timer(150, null);

    t.addActionListener(e -> {
        ((Timer)e.getSource()).stop();

        cells[r1][c1].putClientProperty("type", type2);
        cells[r2][c2].putClientProperty("type", type1);

        cells[r1][c1].setIcon(characterIcons[type2]);
        cells[r2][c2].setIcon(characterIcons[type1]);
    });

    t.setRepeats(false);
    t.start();
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
		if(c+1<8)
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

               int newCandy = randomCandy(i, j);

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
    int j = 0;

    while(j < 8){
        int start = j;
        int t = (int) cells[i][j].getClientProperty("type");

        while(j < 8 && (int)cells[i][j].getClientProperty("type") == t){
            j++;
        }

        int length = j - start;

		if(t != -1 && length >= 3){

			// 🔥 MATCH 5 → special mạnh hơn
			if(length >= 5){
				int specialCol = start + 2; // vị trí giữa
				cells[i][specialCol].putClientProperty("type", t + 10); // ⚡
				mark[i][specialCol] = false; // KHÔNG phá ô này
			}

			// 💣 MATCH 4
			else if(length == 4){
				int specialCol = start + 1;
				cells[i][specialCol].putClientProperty("type", 5); // bomb
				cells[i][specialCol].setIcon(characterIcons[5]); // 👈 THÊMpytgon
				mark[i][specialCol] = false;
			}

			// đánh dấu phá
			for(int k = start; k < j; k++){
				 int t2 = (int) cells[i][k].getClientProperty("type");

				// ❌ không phá bomb và lightning
				if(t2 < 5){
					mark[i][k] = true;
				}
			}
		}
    }
}

    // check dọc (NEW)
	for(int j=0;j<8;j++){
		int i = 0;

		while(i < 8){
			int start = i;
			int t = (int) cells[i][j].getClientProperty("type");

			while(i < 8 && (int)cells[i][j].getClientProperty("type") == t){
				i++;
			}

			int length = i - start;

			if(t != -1 && length >= 3){

				// ⚡ MATCH 5
				if(length >= 5){
					int specialRow = start + 2;
					cells[specialRow][j].putClientProperty("type", t + 10);
					mark[specialRow][j] = false;
				}

				// 💣 MATCH 4
				else if(length == 4){
					int specialRow = start + 1;
					cells[specialRow][j].putClientProperty("type", 5);
					cells[specialRow][j].setIcon(characterIcons[5]);
					mark[specialRow][j] = false;
				}

				// đánh dấu phá
				for(int k = start; k < i; k++){
					int t2 = (int) cells[k][j].getClientProperty("type");

					// ❌ không phá special
					if(t2 < 5){
						mark[k][j] = true;
					}
				}
			}
		}
	}

    // phá + tính điểm
    Timer flash = new Timer(100, null);

    flash.addActionListener(e -> {
        int destroyed = 0;

        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(mark[i][j]){
					destroyed++; 
					// ✨ hiệu ứng nổ nhỏ
					showSmallParticles(i,j);
					showParticles(i, j);
					startParticleAnimation();
                    int type = (int) cells[i][j].getClientProperty("type");
					// ⚡ clear hàng
					if(type >=10){
						clearRow(i);
    				}
					cells[i][j].putClientProperty("type", -1);
					cells[i][j].setIcon(null);    
					}
				}
			}

        if(destroyed > 0){
            combo++; // 🔥 tăng combo

            score += destroyed * 5 * combo; // nhân combo
        }else{
            combo = 0; // reset nếu không ăn được
        }
        updateUIInfo();

        ((Timer)e.getSource()).stop();
        animateGravity();
    });

    flash.setRepeats(false);
    flash.start();
}
    void startParticleAnimation(){

    Timer pt = new Timer(30, null);

    pt.addActionListener(e -> {

        for(Particle p : particles){
            p.update();
        }

        particles.removeIf(p -> p.life <= 0);

        repaint();

        if(particles.isEmpty()){
            ((Timer)e.getSource()).stop();
        }
    });

    pt.start();
}

	void showSmallParticles(int r, int c){

    Point p = SwingUtilities.convertPoint(cells[r][c], 0, 0, this);
    int x = p.x;
    int y = p.y;

    for(int i=0;i<20;i++){ // ít hơn bomb
        particles.add(new Particle(x,y));
    }
}
	
	void explodeCross(int r, int c){

    isProcessing = true;
    playSound("sounds/bom.wav");

    Timer effect = new Timer(80, null);

    final int[] count = {0};

    effect.addActionListener(e -> {

        count[0]++;

        for(int j=0;j<8;j++){
            cells[r][j].setBackground(
                count[0] % 2 == 0 ? Color.YELLOW : Color.ORANGE
            );

            cells[r][j].setSize(65,65); // 👈 zoom nhẹ
        }

        for(int i=0;i<8;i++){
            cells[i][c].setBackground(
                count[0] % 2 == 0 ? Color.YELLOW : Color.ORANGE
            );

            cells[i][c].setSize(65,65);
        }

        // chạy 4 lần rồi nổ thật
        if(count[0] >= 4){
            effect.stop();
            doExplosion(r,c);
        }
    });

    effect.start();
}
	void doExplosion(int r, int c){

    // 💥 tạo hiệu ứng spark
    showParticles(r,c);

    Timer t = new Timer(150, null);

    t.addActionListener(e -> {
        ((Timer)e.getSource()).stop();

        for(int j=0;j<8;j++){
            cells[r][j].putClientProperty("type", -1);
            cells[r][j].setIcon(null);
            cells[r][j].setBackground(null);
            cells[r][j].setSize(60,60); // reset size
        }

        for(int i=0;i<8;i++){
            cells[i][c].putClientProperty("type", -1);
            cells[i][c].setIcon(null);
            cells[i][c].setBackground(null);
            cells[i][c].setSize(60,60);
        }

        animateGravity();
        isProcessing = false; // ✅ THÊM DÒNG NÀY

    });

    t.setRepeats(false);
    t.start();
}
	class Particle {
    int x, y, dx, dy, life;
    Color color;

    Particle(int x, int y){
        this.x = x;
        this.y = y;

        dx = rd.nextInt(11) - 5;
        dy = rd.nextInt(11) - 5;

        life = 20;

        // random màu đẹp
        Color[] colors = {
            Color.YELLOW, Color.PINK, Color.CYAN,
            Color.ORANGE, Color.GREEN
        };
        color = colors[rd.nextInt(colors.length)];
    }

    void update(){
        x += dx;
        y += dy;
        life--;
    }
}
java.util.List<Particle> particles = new java.util.ArrayList<>();
	void showParticles(int r, int c){

    int x = cells[r][c].getX();
    int y = cells[r][c].getY();

    for(int i=0;i<30;i++){
        particles.add(new Particle(x,y));
    }

    Timer pt = new Timer(30, null);

    pt.addActionListener(e -> {

        for(Particle p : particles){
            p.update();
        }

        particles.removeIf(p -> p.life <= 0);

        repaint();

        if(particles.isEmpty()){
            ((Timer)e.getSource()).stop();
        }
    });

    pt.start();
}
@Override
protected void paintComponent(Graphics g){
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;

    for(Particle p : particles){
        g2.setColor(p.color);
        g2.fillOval(p.x, p.y, 6, 6);
    }
}
	void clearRow(int r){

    for(int j=0;j<8;j++){
        cells[r][j].putClientProperty("type", -1);
        cells[r][j].setIcon(null);
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
    	isProcessing = true; // 👈 LOCK
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

				if(hasAnyMatch()){
					animateDestroy();
				} else {
                    combo =0;
					isProcessing = false; // 👈 UNLOCK (RẤT QUAN TRỌNG)
				}
			}
	    });

	    timer.start();
	}
	void nextLevel(){
    isProcessing = true;

    JPanel levelPanel = new JPanel(){
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            g.setColor(new Color(0,0,0,160));
            g.fillRect(0,0,getWidth(),getHeight());
        }
    };

    levelPanel.setLayout(new GridBagLayout());

    JPanel box = new JPanel();
    box.setPreferredSize(new Dimension(280,220));
    box.setBackground(new Color(50,50,100));
    box.setLayout(new GridLayout(4,1,10,10));

    JLabel title = new JLabel("LEVEL UP!", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 22));
    title.setForeground(Color.WHITE);

    JLabel info = new JLabel("Bạn đã đạt level " + (level+1), SwingConstants.CENTER);
    info.setForeground(Color.YELLOW);

    JButton btnContinue = new JButton("▶ Chơi tiếp");
    JButton btnMenu = new JButton("🏠 Về Menu");

    btnContinue.addActionListener(e -> {
        levelPanel.setVisible(false);

        level++;
        moves = 20;
        scoreToReach *= 3;

        resetBoardAfterLevel();

        isLeveling = false;
        isProcessing = false;

        updateUIInfo();
    });

    btnMenu.addActionListener(e -> {
        levelPanel.setVisible(false);
        main.showScreen("MENU");
    });

    box.add(title);
    box.add(info);
    box.add(btnContinue);
    box.add(btnMenu);

    levelPanel.add(box);

    // ✅ QUAN TRỌNG NHẤT
    levelPanel.setOpaque(false);
    main.setGlassPane(levelPanel);
    levelPanel.setVisible(true);
}

void resetBoardAfterLevel(){

    selectedRow = -1;
    selectedCol = -1;

    if(blinkTimer != null) blinkTimer.stop();

    do{
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                spawnOne(i,j);
            }
        }
    }while(hasAnyMatch());

    for(int i=0;i<8;i++){
        for(int j=0;j<8;j++){
            cells[i][j].setEnabled(true);
        }
    }
}

	void spawnOne(int i, int j){
    int newCandy = randomCandy(i, j);
    cells[i][j].putClientProperty("type", newCandy);
    cells[i][j].setIcon(characterIcons[newCandy]);
	}
	int randomCandy(int i, int j){
    int candy;

    do{
        candy = rd.nextInt(5);
    }while(
        (j>=2 &&
         candy == (int)cells[i][j-1].getClientProperty("type") &&
         candy == (int)cells[i][j-2].getClientProperty("type"))
      ||
        (i>=2 &&
         candy == (int)cells[i-1][j].getClientProperty("type") &&
         candy == (int)cells[i-2][j].getClientProperty("type"))
    );

    return candy;
}
JLabel createLabel(String text){
    JLabel lbl = new JLabel(text);

    lbl.setForeground(Color.WHITE);
    lbl.setFont(new Font("Arial", Font.BOLD, 14));

    return lbl;
}
	public GameUI(Main main) {
    this.main = main;
    setLayout(new BorderLayout());
    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setLayout(null);

    add(layeredPane, BorderLayout.CENTER);
    // 🎨 ===== TOP PANEL ĐẸP =====
    JPanel topPanel = new JPanel(){
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            GradientPaint gp = new GradientPaint(
                    0,0,new Color(30,30,60),
                    getWidth(),getHeight(),new Color(80,0,120)
            );

            g2.setPaint(gp);
            g2.fillRect(0,0,getWidth(),getHeight());
        }
    };

    topPanel.setLayout(new BorderLayout());
    topPanel.setPreferredSize(new Dimension(0,80));

    // 📊 PANEL INFO
    JPanel infoPanel = new JPanel(new GridLayout(2,2,10,5));
    infoPanel.setOpaque(false);

    lblScore = createLabel("Score: 0");
    lblMoves = createLabel("Moves: 20");
    lblLevel = createLabel("Level: 1");
    lblTarget = createLabel("Target: 200");

    infoPanel.add(lblScore);
    infoPanel.add(lblMoves);
    infoPanel.add(lblLevel);
    infoPanel.add(lblTarget);

    // 🔙 BUTTON BACK
    JButton btnBack = new JButton("← Menu");
    btnBack.setFocusPainted(false);
    btnBack.setBackground(new Color(255,120,0));
    btnBack.setForeground(Color.WHITE);

    btnBack.addActionListener(e -> main.showScreen("MENU"));

    // 📦 wrap top
    topPanel.add(infoPanel, BorderLayout.CENTER);
    topPanel.add(btnBack, BorderLayout.WEST);

    add(topPanel, BorderLayout.NORTH);

    // 🎮 ===== BOARD =====
    JPanel broadPanel = new GradientPanel();
    broadPanel.setLayout(new GridLayout(8,8,5,5)); // 👈 có khoảng cách đẹp

    // load icon
    for (int i = 0; i <5; i++) {
        characterIcons[i] = loadIcon("images/characters_000"+(i+1)+".png");
    }

    characterIcons[5] = loadIcon("images/bomp.png");        // 💣
    characterIcons[10] = loadIcon("images/lightning.png");  // ⚡

    // tạo grid
    for(int i=0; i<8;i++){
        for(int j=0;j<8;j++){

            cells[i][j] = new JButton();
            cells[i][j].setPreferredSize(new Dimension(60,60));

            // 🎨 style ô
            cells[i][j].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            cells[i][j].setFocusPainted(false);
            cells[i][j].setBackground(new Color(240,240,240));

            int row = i, col = j;
            cells[i][j].addActionListener(e -> handleClick(row,col));

            int candy = randomCandy(i, j);
            cells[i][j].putClientProperty("type", candy);
            cells[i][j].setIcon(characterIcons[candy]);

            broadPanel.add(cells[i][j]);
        }
    }

    add(broadPanel, BorderLayout.CENTER);
    createGameOverPanel();
    updateUIInfo();
	}
}
