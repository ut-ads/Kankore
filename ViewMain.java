/**
 * 
 */
package facss.co.jp.java.src;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PC User
 *
 */
public class ViewMain {

	/**
	 * 
	 */
	public ViewMain() {
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BattleShip bs = new BattleShip();
		
		List<BattleShipDto> dto = new ArrayList<BattleShipDto>();
		
		BattleShipDto bsDto01 = new BattleShipDto();
		
		bsDto01.orgName = "����";
		bsDto01.abbreviation = "�w���x";
		//bsDto01.orgName1 = "����";	
		bsDto01.orgName2 = "��b";
		bsDto01.orgName3 = "�Y��";
		bsDto01.orgName4 = "����";
		dto.add(bsDto01);

		BattleShipDto bsDto02 = new BattleShipDto();
		bsDto02.orgName = "���C";
		bsDto02.abbreviation = "�w���x";
		bsDto02.orgName1 = "���Y";	
		bsDto02.orgName2 = "����";
		bsDto02.orgName3 = "����";
		bsDto02.orgName4 = null;
		dto.add(bsDto02);

		BattleShipDto bsDto03 = new BattleShipDto();
		bsDto03.orgName = "��";
		bsDto03.abbreviation = "�w���V�x";
		bsDto03.orgName1 = "";	
		bsDto03.orgName2 = "�B�u���~���z";
		bsDto03.orgName3 = "��";
		bsDto03.orgName4 = "�d";
		dto.add(bsDto03);
		
		BattleShipDto bsDto04 = new BattleShipDto();
		bsDto04.orgName = "���";
		bsDto04.abbreviation = "�w����x";
		bsDto04.orgName1 = "";	
		bsDto04.orgName2 = "�_��";
		bsDto04.orgName3 = "�߉�";
		dto.add(bsDto04);

		
		
		int startIndex = 0;
		int endIndex = dto.size();
		String[] bsArray = bs.setOrgName(dto, startIndex, endIndex);
		
		System.out.println("�z��̐�:  "+ bsArray.length);
		for(int index = 0 ; index < bsArray.length ; index++){
			System.out.println(bsArray[index]);
		}
	}
}

