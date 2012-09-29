/**
 *  SketchChairs
 *  
 *            ⑁ ⑁ ⑁ ⑁ ⑁ ⑁ ⑁
 *            ⑁ ⑁ ⑁ ⑁ ⑁ ⑁ ⑁
 *            ⑁ ⑁ ⑁ ⑁ ⑁ ⑁ ⑁
 *            ⑁ ⑁ ⑁ ⑁ ⑁ ⑁ ⑁
 *            ⑁ ⑁ ⑁ ⑁ ⑁ ⑁ ⑁
 *  
 */

package cc.sketchchair.core;

import java.util.ArrayList;
import java.util.List;
import ModalGUI.GUIEvent;
import processing.core.PGraphics;

/**
 * Container class to hold multiple designs. 
 * @author gregsaul
 *
 */
public class SketchChairs {

	List<SketchChair> l = new ArrayList<SketchChair>();

	private SketchChair curChair = null;

	void add(SketchChair chair) {
		this.l.add(chair);
		this.setCurChair(chair);
	}

	public void buildCurrentChair(GUIEvent e) {
		this.l.get(this.l.size() - 1).build();
	}

	public void buildCurrentChairLen(GUIEvent e) {
		this.l.get(this.l.size() - 1).buildLen();
	}

	public void buildCurrentChairWidth(GUIEvent e) {
		this.l.get(this.l.size() - 1).buildWidth();
	}

	/**
	* @return the curChair
	*/
	public SketchChair getCurChair() {
		return curChair;
	}

	public void GUIEvent(GUIEvent e) {

		for (int i = 0; i < this.l.size(); i++) {
			// System.out.println("action");

		}
	}

	public void hybernate() {
		for (int i = 0; i < this.l.size(); i++) {
			SketchChair curChair = this.l.get(i);
			curChair.hybernate();
		}
	}

	public void killAll() {

		for (int i = 0; i < this.l.size(); i++) {
			SketchChair curChair = this.l.get(i);
			curChair.destroy();
			this.l.remove(curChair);
		}
		this.setCurChair(null);

	}

	public void killLast() {

		if (this.l.size() > 0) {
			SketchChair curChair = this.l.get(this.l.size() - 1);
			curChair.destroy();
			this.l.remove(curChair);
		}
	}

	public void mouseDragged(int mouseX, int mouseY) {

		if (getCurChair() != null)
			this.getCurChair().mouseDragged(mouseX, mouseY);
	}

	public void mousePressed(float mouseX, float mouseY) {
		if (getCurChair() != null)
			this.getCurChair().mousePressed(mouseX, mouseY);

	}

	void mouseReleased(float mouseX, float mouseY) {

		if (getCurChair() != null)
			getCurChair().mouseReleased(mouseX, mouseY);
	}

	

	public void mouseDoubleClick(int mouseX, int mouseY) {
		if (getCurChair() != null)
			getCurChair().mouseDoubleClick(mouseX, mouseY);		
	}
	void render(PGraphics g) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchChair curChair = this.l.get(i);
			curChair.render(g);
		}
	}
	
	public void renderPickBuffer(PGraphics pickBuffer) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchChair curChair = this.l.get(i);
			curChair.renderPickBuffer(pickBuffer);
		}		
	}
	
	
	

	public void SelectNodes(int mouseX, int mouseY) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchChair curChair = this.l.get(i);
			curChair.selectNodes(mouseX, mouseY);
		}
	}

	/**
	 * @param curChair the curChair to set
	 */
	private void setCurChair(SketchChair curChair) {
		this.curChair = curChair;
	}

	void update() {
		for (int i = 0; i < this.l.size(); i++) {
			SketchChair curChair = this.l.get(i);
			if (curChair.destroy){
				this.l.remove(i);
				
				//is this the current Chair?
				if(curChair.equals(this.getCurChair()))
					this.setCurChair(null);
			}
			else
				curChair.update();
		}

	}




	
}
