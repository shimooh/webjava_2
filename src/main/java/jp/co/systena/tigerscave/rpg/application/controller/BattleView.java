package jp.co.systena.tigerscave.rpg.application.controller;

import java.util.List;
import java.util.Random;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import jp.co.systena.tigerscave.rpg.application.model.CharaCreate;
import jp.co.systena.tigerscave.rpg.application.model.Character;
import jp.co.systena.tigerscave.rpg.application.model.Goblin;
import jp.co.systena.tigerscave.rpg.application.model.Monster;
import jp.co.systena.tigerscave.rpg.application.model.Party;

@Controller
public class BattleView {

  Party mParty = new Party();

  @Autowired
  HttpSession session;

  @RequestMapping("/CharacterCreate")
  public ModelAndView showCharacterCreate() {
    ModelAndView mav = new ModelAndView();
    mav.addObject("CharacterForm", new CharaCreate());
    mav.setViewName("CharacterCreateView");
    return mav;
  }

  @RequestMapping(value = "/CreateCompleted", method = RequestMethod.POST)
  public ModelAndView characterCreate(HttpSession session, ModelAndView mav,
      CharaCreate charaCreate) {
    int id = 1;
    if (session.getAttribute("partyList") != null) {
      mParty = (Party) session.getAttribute("partyList");
      id = mParty.getPartyList().size() + 1;
    }
    final int DEFAULT_HP = 100;
    Character createdCharacter =
        new Character(id, charaCreate.getJob(), DEFAULT_HP, charaCreate.getName());

    mParty.addPartyMember(createdCharacter);

    session.setAttribute("partyList", mParty);

    mav.addObject("party", mParty);

    // テンプレート名を設定
    mav.setViewName("CharacterCreatedView");

    return mav;
  }

  @RequestMapping(value = "/Command", method = RequestMethod.POST)
  public ModelAndView commandSelect(HttpSession session, ModelAndView mav) {
    mParty = (Party) session.getAttribute("partyList");

    // コマンド画面を開いたところで全キャラのコマンドを"未選択"に設定
    for (Character chara : mParty.getPartyList()) {
      chara.setCommand("未選択");
    }

    mav.addObject("party", mParty);
    mav.setViewName("CommandView");
    return mav;
  }

  @RequestMapping(value = "/SelectCommand", method = RequestMethod.POST)
  public ModelAndView commandSelect(@ModelAttribute("selectcommand") String selectedCommand,
      @ModelAttribute("memberId") int selectCharaId, HttpSession session, ModelAndView mav) {
    mParty = (Party) session.getAttribute("partyList");
    List<Character> partyList = mParty.getPartyList();

    // 選ばれたコマンドの表示する文言をキャラクタにセット
    Character commandSelectChara = partyList.get(selectCharaId - 1);
    switch (selectedCommand) {
      case "たたかう":
        commandSelectChara.setCommand(commandSelectChara.getJob().fight());
        commandSelectChara.setCommandId(commandSelectChara.COMMAND_FIGHT);
        break;
      case "かいふく":
        commandSelectChara.setCommand(commandSelectChara.getJob().heal());
        commandSelectChara.setCommandId(commandSelectChara.COMMAND_HEAL);

        // ⑧ 回復実行
        commandSelectChara.executeHeal();
        break;
    }

 // 全員のコマンド選択が終わっているか確認
    Party commandUnselectedCharacterList = new Party();
    for (Character chara : mParty.getPartyList()) {
      if (chara.getCommand() == "未選択") {
        commandUnselectedCharacterList.addPartyMember(chara);
      }
    }

    if (commandUnselectedCharacterList.getPartyList().size() != 0) {
      // コマンド選択が終わっていないキャラがいるため、未選択キャラだけ残したCommandViewを再表示
      mav.addObject("party", commandUnselectedCharacterList);
      mav.setViewName("CommandView");
    } else {
      // コマンド選択が終わっているようなので戦闘結果画面を表示

      // 初回はモンスターを生成する、初回以外はsessionから取得する
      Monster monster = (Monster) session.getAttribute("monster");
      if (monster == null) {
        monster = new Goblin("ゴブリンA");
      }

      // モンスターにダメージを与える
      List<Character> character = mParty.getPartyList();
      int damageQuantity = 0;

      for (Character member : character) {
        if (member.getCommandId() == member.COMMAND_FIGHT) {
          damageQuantity += 10;
        }

      }
      monster.takeDamage(damageQuantity);

      session.setAttribute("monster", monster);

      // ⑧モンスターが攻撃してくる
      // TODO:パーティメンバーが倒れても特に行動できないわけでもないし被攻撃対象にもなってしまう

      // モンスターがパーティメンバーの誰かを指定する
      Random rand = new Random();
      int attackOpponentId = rand.nextInt(character.size());

      // モンスターの基本攻撃力を取得
      int attackPower = monster.getAttackPower();

      // 威力を0~2倍までランダムに設定
      attackPower *= rand.nextInt(3);

      // 指定したメンバーのHPを減らす
      int remainingHP = character.get(attackOpponentId).getHp();
      remainingHP -= attackPower;
      character.get(attackOpponentId).setHp(remainingHP);

      // モンスターの攻撃文言を準備
      monster.fight(character.get(attackOpponentId).getName(), attackPower);

      // 減った状態を反映させるためにsessionに保存する
      mParty.setPartyList(character);
      session.setAttribute("party", mParty);

      // プレイヤーのパーティを渡す
      mav.addObject("party", mParty);

      mav.addObject("monster", monster);
      mav.setViewName("BattleView");
    }
    return mav;
  }

}
