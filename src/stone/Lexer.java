package stone;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public static final String regexPat = "\\s*((//.*)|([0-9]+)|(\"(\\\\\"|\\\\\\\\|\\\\n|[^\"])*\")"
            + "|[A-Z_a-z][A-Z_a-z0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct})?";
    private Pattern pattern = Pattern.compile(regexPat);
    private ArrayList<Token> queue = new ArrayList<>();
    private boolean hasMore;
    private LineNumberReader reader;

    public Lexer(Reader r) {
        hasMore = true;
        reader = new LineNumberReader(r);
    }

    protected void readLine() throws ParseException {
        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new ParseException(e);
        }
        if (line == null) {
            return;
        }
        int lineNo = reader.getLineNumber();
        Matcher matcher = pattern.matcher(line);
        matcher.useTransparentBounds(true).useAnchoringBounds(false);
        int pos = 0;
        int endPos = line.length();
        while (pos < endPos) {
            matcher.region(pos, endPos);
            if (matcher.lookingAt()) {
                addToken(lineNo, matcher);
                pos = matcher.end();
            } else {
                throw new ParseException("bad token at line " + lineNo);
            }
        }
        queue.add(new IdToken(lineNo, Token.EOL));
    }

    protected void addToken(int lineNo, Matcher matcher) {
        String m = matcher.group(1);
        if (m != null) { // if not a space
            if (matcher.group(2) == null) { // not a comment
                Token token;
                if (matcher.group(3) != null) {
                    token = new NumToken(lineNo, Integer.parseInt(m));
                } else if (matcher.group(4) != null) {
                    token = new StrToken(lineNo, toStringLiteral(m);)
                } else {
                    token = new IdToken(lineNo, m);
                }
                queue.add(token);
            }
        }
    }

    protected String toStringLiteral(String rawStr) {
        StringBuilder builder = new StringBuilder();
        int len = rawStr.length() - 1;
        for (int i = 1; i < len; i++) {
            char c = rawStr.charAt(i);
            if (c == '\\' && (i + 1) < len) {
                char temp = rawStr.charAt(i + 1);
                if (temp == '\\' || temp == '"') {
                    c = rawStr.charAt(++i);
                }else if (temp == 'n') {
                    ++i;
                    c = '\n';
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }
}
