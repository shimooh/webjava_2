package jp.co.systena.tigerscave.rpg.application.model;

public class MagicCaster extends Job{
//魔法使いクラス
 @Override
 public String fight() {
   return "まほうで攻撃した！";
 }

 @Override
 public String heal() {
   return "まほうで回復した！";
 }
}
