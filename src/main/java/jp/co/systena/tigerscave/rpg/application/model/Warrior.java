package jp.co.systena.tigerscave.rpg.application.model;

public class Warrior extends Job {
//戦士クラス
 @Override
 public String fight() {
   return "剣で攻撃した！";
 }

 @Override
 public String heal() {
   return "やくそうで回復した！";
 }
}
