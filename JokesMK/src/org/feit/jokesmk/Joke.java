package org.feit.jokesmk;

public class Joke {
	public long id;
	public String name;
	public String text;
	public long category_id;
	public int rating;
	public boolean favorite;
	public int views;
	
	public String getIntro() {
		int max = text.length();
		return String.format("%s...", text.substring(0, Math.min(max, 50)));
	}
	
	public String getShareLink(String categorySlug) {
		return String.format("http://vicoteka.mk/vicovi/%s/%s", categorySlug, toLatinName());
	}
	
	private String toLatinName() {
		StringBuilder sb = new StringBuilder();
		char[] name = this.name.toLowerCase().toCharArray();
		for(char c : name) {
			sb.append(toLatin(c));
		}
		return sb.toString();
	}
	
	private static String toLatin(char c) {
		switch(c){
		case 'а':
			return "a";
		case 'б':
			return "b";
		case 'в':
			return "v";
		case 'г':
			return "g";
		case 'д':
			return "d";
		case 'ѓ':
			return "gj";
		case 'е':
			return "e";
		case 'ж':
			return "z";
		case 'з':
			return "z";
		case 'ѕ':
			return "dz";
		case 'и':
			return "i";
		case 'ј':
			return "j";
		case 'к':
			return "k";
		case 'л':
			return "l";
		case 'љ':
			return "lj";
		case 'м':
			return "m";
		case 'н':
			return "n";
		case 'њ':
			return "nj";
		case 'о':
			return "o";
		case 'п':
			return "p";
		case 'р':
			return "r";
		case 'с':
			return "s";
		case 'т':
			return "t";
		case 'ќ':
			return "kj";
		case 'у':
			return "u";
		case 'ф':
			return "f";
		case 'х':
			return "h";
		case 'ц':
			return "c";
		case 'ч':
			return "ch";
		case 'џ':
			return "dj";
		case 'ш':
			return "sh";
		case ' ':
			return "-";
		}
		return "";
		
	}
}
