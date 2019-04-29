import tkinter as tk

class Map(tk.Frame):
    
    
    def __init__(self, master, *args, **kwargs):
        tk.Frame.__init__(self, master, *args, **kwargs)
        widthSquare=50
        heightSquare=50
        self.master = master
        self.s1 = tk.Frame(self, background="grey", width=widthSquare, height=heightSquare)
        self.s2 = tk.Frame(self, background="white", width=widthSquare, height=heightSquare)
        self.s3 = tk.Frame(self, background="grey", width=widthSquare, height=heightSquare)
        self.s4 = tk.Frame(self, background="white", width=widthSquare, height=heightSquare)
        self.s5 = tk.Frame(self, background="grey", width=widthSquare, height=heightSquare)
        self.s6 = tk.Frame(self, background="white", width=widthSquare, height=heightSquare)
        self.s7 = tk.Frame(self, background="grey", width=widthSquare, height=heightSquare)
        self.s8 = tk.Frame(self, background="white", width=widthSquare, height=heightSquare)
        
        self.s9 = tk.Frame(self, background="white", width=widthSquare, height=heightSquare)
        self.s10 = tk.Frame(self, background="grey", width=widthSquare, height=heightSquare)
        self.s11 = tk.Frame(self, background="white", width=widthSquare, height=heightSquare)
        self.s12 = tk.Frame(self, background="grey", width=widthSquare, height=heightSquare)
        self.s13 = tk.Frame(self, background="white", width=widthSquare, height=heightSquare)
        self.s14 = tk.Frame(self, background="grey", width=widthSquare, height=heightSquare)
        self.s15 = tk.Frame(self, background="white", width=widthSquare, height=heightSquare)
        self.s16 = tk.Frame(self, background="grey", width=widthSquare, height=heightSquare)
        
        self.s1.grid(row=0, column=0, sticky="nsew")
        self.s2.grid(row=0, column=1, sticky="nsew")
        self.s3.grid(row=0, column=2,  sticky="nsew")
        self.s4.grid(row=0, column=3, sticky="nsew")
        self.s5.grid(row=0, column=4,  sticky="nsew")
        self.s6.grid(row=0, column=5, sticky="nsew")
        self.s7.grid(row=0, column=6, sticky="nsew")
        self.s8.grid(row=0, column=7, columnspan=8, sticky="nsew")
        
        self.s9.grid(row=1, column=0, sticky="nsew")
        self.s10.grid(row=1, column=1, sticky="nsew")
        self.s11.grid(row=1, column=2,  sticky="nsew")
        self.s12.grid(row=1, column=3, sticky="nsew")
        self.s13.grid(row=1, column=4,  sticky="nsew")
        self.s14.grid(row=1, column=5, sticky="nsew")
        self.s15.grid(row=1, column=6, sticky="nsew")
        self.s16.grid(row=1, column=7, columnspan=8, sticky="nsew")
        

        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=1)
        self.grid_columnconfigure(1, weight=1)
        self.grid_columnconfigure(2, weight=1)
        self.grid_columnconfigure(3, weight=1)
        self.grid_columnconfigure(4, weight=1)
        self.grid_columnconfigure(5, weight=1)
        self.grid_columnconfigure(6, weight=1)
        
if __name__ == "__main__":
    root = tk.Tk()
    Map(root).pack(side="top", fill="both", expand=True)
    root.mainloop()