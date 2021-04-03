import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GuiAuc extends JFrame{
	private AucAgent Auc;
	private JTextField productTitle,productPrice;
	private JPanel myPanel,myPanel2,myPanel3;
	
	public GuiAuc(AucAgent testAuc)
	{
		
		Auc=testAuc;
		
		myPanel3=new JPanel();
		JLabel testLabel=new JLabel("Adding products to List");
		testLabel.setFont(new Font("Serif", Font.BOLD, 22));
		myPanel3.add(testLabel);
		this.getContentPane().add(myPanel3, BorderLayout.NORTH);
		
		myPanel = new JPanel();
		myPanel.setLayout(new GridLayout(2, 2));
		JLabel testLabel2=new JLabel("Product Name:");
		testLabel2.setFont(new Font("Serif", Font.PLAIN, 20));
		myPanel.add(testLabel2);
		productTitle = new JTextField(18);
		myPanel.add(productTitle);
		
		JLabel testLabel3=new JLabel("Product Price:");
		testLabel3.setFont(new Font("Serif", Font.PLAIN, 20));
		myPanel.add(testLabel3);
		productPrice = new JTextField(18);
		myPanel.add(productPrice);
		this.getContentPane().add(myPanel, BorderLayout.CENTER);
		
		JButton but1 = new JButton("Add to List");
		but1.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    
                        try
                        {
                        	String test_product = productTitle.getText();
                            int test_price = Integer.parseInt(productPrice.getText());
	                        Auc.addToList(test_product, test_price);
	                        productTitle.setText("");
	                        productPrice.setText("");
                        }
                        catch (Exception e) 
                        {
                           e.printStackTrace();
                        }
                }
            } );
		myPanel2 = new JPanel();
		myPanel2.add(but1);
		this.getContentPane().add(myPanel2, BorderLayout.SOUTH);
		}
		
		public void showGui()
		{
			this.pack();
			this.setLocationRelativeTo(null);
			this.setTitle("Add products to List");
			this.setVisible(true);	
		}
		
		
	}
	
	

