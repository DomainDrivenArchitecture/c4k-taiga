
```mermaid
C4Context
    title c4k-webserver
    Boundary(website, "website") {
        System(website_ing1, "ingress f. host meissa-gmbh.de")
        System(website_ing2, "ingress f. host meissa.de")
        Boundary(website_pod, "website pod"){
            Boundary(aaa, "website container") {
                System(ws, "webserver")
                SystemDb(file_html, "static html")
                Rel(ws, file_html, "file ro")
            }
            Boundary(aab, "cron build website") {
                System(git_clone, "git clone/pull & lein ring server & copy to static html")
                SystemDb(file_git, "git repo")
                Rel(git_clone, file_git, "file rw")
                Rel(git_clone, file_html, "file rw")
            }           
        }
        Rel(website_ing1, ws, "http")
        Rel(website_ing2, ws, "http")        
    }

```
[![](https://mermaid.ink/img/pako:eNqNU8tugzAQ_JWVD1UqJaka5cSxSX-guSIhgxewamxkL01RxL_XQFExSdr6gtee2ccMvrDMCGQRO-wPRhN-UqzBL5KkELL9--aMqUP7gXa8eDGNFty2q_5cEq4hZt_bmD3CZUT169Q6wmrCJVIXzz3Yfy06B_kWSuMIKpTO8U1RpeVW9Dl-y7C7n2FJXjaa1EbMmgUfevys34DEOZ-DM68Nl9qLEM647NRNpFGxeT8h9JiucqkwKalSPccRJ5lBH95kvaEasgekPgBrrgjd3aHSnpZZoyFtpBJw27nFVIWkJFNGD177AIbgqW6UggdQKDVY7wmMQ_ujzNQtkIG_hgql8JmnAhZrc1eGWTtz3ijG-d-0UMRrXjfb_9zMpB0sCX7u0f6SqA6yLYC7BTAs4guwNavQVlwK_y4HW2JGJVbepshvBea8URSzWHce2tSCE74KScayKOfK4Zrxhsyp1RmLyDY4gY6SF5ZX42H3BTITMPU)](https://mermaid.live/edit#pako:eNqNU8tugzAQ_JWVD1UqJaka5cSxSX-guSIhgxewamxkL01RxL_XQFExSdr6gtee2ccMvrDMCGQRO-wPRhN-UqzBL5KkELL9--aMqUP7gXa8eDGNFty2q_5cEq4hZt_bmD3CZUT169Q6wmrCJVIXzz3Yfy06B_kWSuMIKpTO8U1RpeVW9Dl-y7C7n2FJXjaa1EbMmgUfevys34DEOZ-DM68Nl9qLEM647NRNpFGxeT8h9JiucqkwKalSPccRJ5lBH95kvaEasgekPgBrrgjd3aHSnpZZoyFtpBJw27nFVIWkJFNGD177AIbgqW6UggdQKDVY7wmMQ_ujzNQtkIG_hgql8JmnAhZrc1eGWTtz3ijG-d-0UMRrXjfb_9zMpB0sCX7u0f6SqA6yLYC7BTAs4guwNavQVlwK_y4HW2JGJVbepshvBea8URSzWHce2tSCE74KScayKOfK4Zrxhsyp1RmLyDY4gY6SF5ZX42H3BTITMPU)