package trrp.lab1.model;

public class Comment {

    private String id;
    private String _content;

    public Comment(String id, String value) {
        this.id = id;
        this._content = value;
    }

    public Comment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String get_content() {
        return _content;
    }

    public void set_content(String _content) {
        this._content = _content;
    }
}
