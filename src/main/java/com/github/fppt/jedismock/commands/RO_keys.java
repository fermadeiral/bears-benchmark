package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.ArrayList;
import java.util.List;

class RO_keys extends AbstractRedisOperation {
    RO_keys(RedisBase base, List<Slice> params) {
        super(base, params, 1, null, null);
    }

    Slice response() {
        List<Slice> matchingKeys = new ArrayList<>();
        String regex = createRegexFromGlob(new String(params().get(0).data()));

        base().keys().forEach(keyData -> {
            String key = new String(keyData.data());
            if(key.matches(regex)){
                matchingKeys.add(Response.bulkString(keyData));
            }
        });

        return Response.array(matchingKeys);
    }

    private static String createRegexFromGlob(String glob)
    {
        StringBuilder out = new StringBuilder("^");
        for(int i = 0; i < glob.length(); ++i)
        {
            final char c = glob.charAt(i);
            switch(c)
            {
                case '*':
                    out.append(".*");
                    break;
                case '?':
                    out.append('.');
                    break;
                case '.':
                    out.append("\\.");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(c);
            }
        }
        out.append('$');
        return out.toString();
    }
}
